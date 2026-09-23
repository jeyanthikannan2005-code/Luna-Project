package com.lunette.gifts.controller;

import com.lunette.gifts.dto.OrderDto;
import com.lunette.gifts.entity.*;
import com.lunette.gifts.repository.CouponRepository;
import com.lunette.gifts.repository.ReviewRepository;
import com.lunette.gifts.service.FileStorageService;
import com.lunette.gifts.service.OrderService;
import com.lunette.gifts.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/store")
public class StoreController {

    private final ProductService productService;
    private final OrderService orderService;
    private final FileStorageService fileStorageService;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;

    public StoreController(ProductService productService,
                           OrderService orderService,
                           FileStorageService fileStorageService,
                           CouponRepository couponRepository,
                           ReviewRepository reviewRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.fileStorageService = fileStorageService;
        this.couponRepository = couponRepository;
        this.reviewRepository = reviewRepository;
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @GetMapping("/products/{slug}")
    public ResponseEntity<?> getProductBySlug(@PathVariable String slug) {
        try {
            return ResponseEntity.ok(productService.getProductBySlug(slug));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "category", defaultValue = "general") String category) {
        try {
            Map<String, Object> result = fileStorageService.storeFile(file, category);
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not upload file: " + e.getMessage()));
        }
    }

    @PostMapping("/order")
    public ResponseEntity<?> placeOrder(@RequestBody OrderDto.OrderCreateRequest req, Authentication auth) {
        try {
            String username = (auth != null && auth.getPrincipal() instanceof UserAccount user) ? user.getUsername() : null;
            Order order = orderService.createOrder(req, username, false);
            return ResponseEntity.ok(Map.of(
                    "orderNumber", order.getOrderNumber(),
                    "totalAmount", order.getTotalAmount(),
                    "deliveryCharge", order.getDeliveryCharge(),
                    "status", order.getOrderStatus(),
                    "message", "Thank you! Your personalized order has been placed."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Error creating order: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/track/{orderNumber}")
    public ResponseEntity<?> trackOrder(@PathVariable String orderNumber) {
        Optional<Order> orderOpt = orderService.getOrderByNumber(orderNumber.trim().toUpperCase());
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Order not found with number: " + orderNumber));
        }

        Order o = orderOpt.get();
        return ResponseEntity.ok(Map.of(
                "orderNumber", o.getOrderNumber(),
                "customerName", o.getCustomerName(),
                "orderStatus", o.getOrderStatus(),
                "paymentStatus", o.getPaymentStatus(),
                "deliveryZone", o.getDeliveryZone(),
                "deliveryCharge", o.getDeliveryCharge(),
                "totalAmount", o.getTotalAmount(),
                "createdAt", o.getCreatedAt(),
                "updatedAt", o.getUpdatedAt(),
                "history", o.getStatusHistory()
        ));
    }

    @PostMapping("/coupon/validate")
    public ResponseEntity<?> validateCoupon(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.get("code");
        Object subtotalObj = payload.get("subtotal");

        if (code == null || code.isBlank() || subtotalObj == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Code and subtotal are required"));
        }

        BigDecimal subtotal = new BigDecimal(subtotalObj.toString());
        Optional<Coupon> couponOpt = couponRepository.findByCodeIgnoreCaseAndActiveTrue(code.trim());

        if (couponOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Invalid or expired coupon code"));
        }

        Coupon coupon = couponOpt.get();
        if (!coupon.isValid(subtotal)) {
            return ResponseEntity.badRequest().body(Map.of("valid", false, "error", "Coupon conditions not met or usage limit reached"));
        }

        BigDecimal discount;
        if ("PERCENTAGE".equalsIgnoreCase(coupon.getDiscountType())) {
            discount = subtotal.multiply(coupon.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
        } else {
            discount = coupon.getDiscountValue();
        }

        return ResponseEntity.ok(Map.of(
                "valid", true,
                "code", coupon.getCode(),
                "discount", discount,
                "discountType", coupon.getDiscountType()
        ));
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getApprovedReviews() {
        return ResponseEntity.ok(reviewRepository.findByApprovedTrueOrderByCreatedAtDesc());
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> submitReview(@RequestBody Map<String, Object> payload) {
        try {
            Long productId = Long.valueOf(payload.get("productId").toString());
            String customerName = (String) payload.get("customerName");
            int rating = Integer.parseInt(payload.get("rating").toString());
            String comment = (String) payload.get("comment");
            String imageUrl = (String) payload.get("imageUrl");

            Product product = productService.getProductById(productId);
            Review review = new Review(product, customerName, rating, comment, imageUrl, true);
            reviewRepository.save(review);

            return ResponseEntity.ok(Map.of("message", "Review submitted successfully! Thank you for sharing your memory."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Could not submit review: " + e.getMessage()));
        }
    }
}
