package com.lunette.gifts.service;

import com.lunette.gifts.dto.OrderDto;
import com.lunette.gifts.entity.*;
import com.lunette.gifts.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final CouponRepository couponRepository;
    private final UserAccountRepository userRepository;

    private final BigDecimal maduraiDeliveryCharge;
    private final BigDecimal outsideDeliveryCharge;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        ProductVariantRepository variantRepository,
                        OrderStatusHistoryRepository statusHistoryRepository,
                        CouponRepository couponRepository,
                        UserAccountRepository userRepository,
                        @Value("${lunette.delivery.madurai:70}") BigDecimal maduraiDeliveryCharge,
                        @Value("${lunette.delivery.outside:100}") BigDecimal outsideDeliveryCharge) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
        this.maduraiDeliveryCharge = maduraiDeliveryCharge;
        this.outsideDeliveryCharge = outsideDeliveryCharge;
    }

    public Order createOrder(OrderDto.OrderCreateRequest req, String authenticatedUsername, boolean staffAssisted) {
        if (req.getItems() == null || req.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Please add items to checkout.");
        }

        Order order = new Order();
        order.setCustomerName(req.getCustomerName());
        order.setCustomerPhone(req.getCustomerPhone());
        order.setCustomerEmail(req.getCustomerEmail());
        order.setShippingAddress(req.getShippingAddress());
        order.setPinCode(req.getPinCode());
        order.setCity(req.getCity());
        order.setDistrict(req.getDistrict());
        order.setState(req.getState());
        order.setOrderNotes(req.getOrderNotes());
        order.setPaymentReference(req.getPaymentReference());
        order.setPaymentProofUrl(req.getPaymentProofUrl());
        order.setStaffAssisted(staffAssisted);
        if (staffAssisted) {
            order.setCreatedByStaffUsername(authenticatedUsername);
        }

        // Link customer account if exists
        if (authenticatedUsername != null && !staffAssisted) {
            userRepository.findByUsername(authenticatedUsername).ifPresent(order::setCustomer);
        } else if (req.getCustomerEmail() != null) {
            userRepository.findByEmail(req.getCustomerEmail()).ifPresent(order::setCustomer);
        }

        // Calculate delivery charge ONCE per order
        String zone = determineDeliveryZone(req.getPinCode(), req.getDeliveryZone());
        order.setDeliveryZone(zone);
        BigDecimal delivery = "MADURAI".equalsIgnoreCase(zone) ? maduraiDeliveryCharge : outsideDeliveryCharge;
        order.setDeliveryCharge(delivery);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderDto.OrderItemRequest itemReq : req.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found: " + itemReq.getProductId()));

            int quantity = Math.max(1, itemReq.getQuantity());
            if (quantity < product.getMinQuantity()) {
                throw new IllegalArgumentException("Minimum quantity for " + product.getName() + " is " + product.getMinQuantity());
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(quantity);
            orderItem.setCustomizationSelected(itemReq.isCustomizationSelected());
            orderItem.setCustomizationInstructions(itemReq.getCustomizationInstructions());

            BigDecimal unitPrice = product.getBasePrice();
            int requiredPhotos = 1;

            // Handle Product Variants (e.g. Photo Frames: 5x5, 4x6, 5x7, A4)
            if (itemReq.getVariantId() != null) {
                ProductVariant variant = variantRepository.findById(itemReq.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + itemReq.getVariantId()));
                orderItem.setVariantName(variant.getName());
                orderItem.setVariantDimensions(variant.getDimensions());
                unitPrice = variant.getPrice();
                // Admin-configured required photo count from DB!
                requiredPhotos = variant.getRequiredPhotos();
            } else if (!product.getVariants().isEmpty()) {
                ProductVariant firstVariant = product.getVariants().get(0);
                orderItem.setVariantName(firstVariant.getName());
                orderItem.setVariantDimensions(firstVariant.getDimensions());
                unitPrice = firstVariant.getPrice();
                requiredPhotos = firstVariant.getRequiredPhotos();
            } else {
                orderItem.setVariantName(itemReq.getVariantName() != null ? itemReq.getVariantName() : "Standard");
            }

            // Calculate item price according to category rules
            BigDecimal itemTotal;
            if ("PHOTO_CARDS".equalsIgnoreCase(product.getCategory())) {
                // ₹8 per card
                itemTotal = product.getBasePrice().multiply(BigDecimal.valueOf(quantity));
                requiredPhotos = quantity; // 1 photo per card
            } else if ("RING_ALBUMS".equalsIgnoreCase(product.getCategory())) {
                // Ring price ₹20 + (quantity * ₹8)
                itemTotal = product.getRingPrice().add(product.getBasePrice().multiply(BigDecimal.valueOf(quantity)));
                requiredPhotos = quantity; // 1 photo per card
            } else if ("LED_POLAROIDS".equalsIgnoreCase(product.getCategory())) {
                // Light price ₹150 + (quantity * ₹7)
                itemTotal = product.getLightPrice().add(product.getBasePrice().multiply(BigDecimal.valueOf(quantity)));
                requiredPhotos = quantity; // 1 photo per card
            } else {
                // PHOTO_FRAMES
                itemTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
            }

            BigDecimal custCharge = BigDecimal.ZERO;
            if (itemReq.isCustomizationSelected()) {
                custCharge = product.getCustomizationPrice();
                itemTotal = itemTotal.add(custCharge);
            }
            orderItem.setCustomizationCharge(custCharge);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setItemSubtotal(itemTotal);
            orderItem.setRequiredPhotos(requiredPhotos);

            // Customer Photos Attachment
            int uploadedCount = 0;
            if (itemReq.getPhotoUrls() != null) {
                for (int i = 0; i < itemReq.getPhotoUrls().size(); i++) {
                    String url = itemReq.getPhotoUrls().get(i);
                    String origName = (itemReq.getOriginalFilenames() != null && itemReq.getOriginalFilenames().size() > i)
                            ? itemReq.getOriginalFilenames().get(i) : "photo_" + (i + 1);
                    CustomerPhoto cp = new CustomerPhoto(orderItem, url, origName, "image/jpeg", 0, "PRINT_PHOTO", i + 1);
                    orderItem.addPhoto(cp);
                    uploadedCount++;
                }
            }

            // Customization Reference Image Attachment
            if (itemReq.isCustomizationSelected() && itemReq.getCustomizationReferenceUrl() != null && !itemReq.getCustomizationReferenceUrl().isBlank()) {
                String refOrig = itemReq.getCustomizationReferenceFilename() != null ? itemReq.getCustomizationReferenceFilename() : "customization_ref.jpg";
                CustomerPhoto refPhoto = new CustomerPhoto(orderItem, itemReq.getCustomizationReferenceUrl(), refOrig, "image/jpeg", 0, "CUSTOMIZATION_REFERENCE", 0);
                orderItem.addPhoto(refPhoto);
            }

            orderItem.setUploadedPhotos(uploadedCount);
            order.addItem(orderItem);

            subtotal = subtotal.add(itemTotal);
        }

        order.setSubtotal(subtotal);

        // Apply coupon if valid
        BigDecimal discount = BigDecimal.ZERO;
        if (req.getCouponCode() != null && !req.getCouponCode().isBlank()) {
            Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(req.getCouponCode().trim());
            if (couponOpt.isPresent()) {
                Coupon coupon = couponOpt.get();
                if (coupon.isValid(subtotal)) {
                    if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
                        discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    } else {
                        discount = coupon.getDiscountValue();
                    }
                    coupon.setUsageCount(coupon.getUsageCount() + 1);
                    couponRepository.save(coupon);
                    order.setCouponCode(coupon.getCode());
                }
            }
        }
        order.setDiscount(discount);

        BigDecimal total = subtotal.subtract(discount).add(delivery);
        order.setTotalAmount(total.max(BigDecimal.ZERO));

        // Initial statuses
        order.setPaymentStatus("PENDING");
        order.setOrderStatus("ORDER_PLACED");

        // Temporary order number; will be finalized after save
        order.setOrderNumber("LG-TEMP-" + System.currentTimeMillis());
        Order saved = orderRepository.save(order);

        // Format official order number: LG-10000 + ID
        saved.setOrderNumber("LG-" + (10000 + saved.getId()));
        saved = orderRepository.save(saved);

        // Record initial status history
        OrderStatusHistory history = new OrderStatusHistory(
                saved,
                null,
                "ORDER_PLACED",
                authenticatedUsername != null ? authenticatedUsername : "Customer",
                "Order placed successfully."
        );
        statusHistoryRepository.save(history);

        return saved;
    }

    public Order updateOrderStatus(Long orderId, String newStatus, String notes, String changedByUsername) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        String oldStatus = order.getOrderStatus();
        order.setOrderStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());

        OrderStatusHistory history = new OrderStatusHistory(order, oldStatus, newStatus, changedByUsername, notes);
        statusHistoryRepository.save(history);

        return orderRepository.save(order);
    }

    public Order verifyPayment(Long orderId, String paymentStatus, String notes, String verifiedBy) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

        order.setPaymentStatus(paymentStatus);
        if ("CONFIRMED".equalsIgnoreCase(paymentStatus)) {
            if ("ORDER_PLACED".equals(order.getOrderStatus()) || "PAYMENT_PENDING".equals(order.getOrderStatus())) {
                order.setOrderStatus("PAYMENT_CONFIRMED");
            }
        }
        order.setUpdatedAt(LocalDateTime.now());

        OrderStatusHistory history = new OrderStatusHistory(
                order,
                order.getOrderStatus(),
                order.getOrderStatus(),
                verifiedBy,
                "Payment marked as " + paymentStatus + ". " + (notes != null ? notes : "")
        );
        statusHistoryRepository.save(history);

        return orderRepository.save(order);
    }

    public Optional<Order> getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Order> getOrdersByCustomerUsername(String username) {
        return userRepository.findByUsername(username)
                .map(orderRepository::findByCustomerOrderByCreatedAtDesc)
                .orElse(List.of());
    }

    public List<Order> searchOrders(String query) {
        if (query == null || query.isBlank()) {
            return orderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orderRepository.searchOrders(query.trim());
    }

    public String determineDeliveryZone(String pinCode, String explicitZone) {
        if (explicitZone != null && !explicitZone.isBlank()) {
            return explicitZone.toUpperCase().contains("MADURAI") && !explicitZone.toUpperCase().contains("OUTSIDE")
                    ? "MADURAI" : "OUTSIDE_MADURAI";
        }
        if (pinCode != null && pinCode.trim().length() >= 3) {
            String prefix = pinCode.trim().substring(0, 3);
            // Madurai postal code prefixes typically start with 625
            if ("625".equals(prefix)) {
                return "MADURAI";
            }
        }
        return "OUTSIDE_MADURAI";
    }
}
