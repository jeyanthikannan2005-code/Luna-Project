package com.lunette.gifts.config;

import com.lunette.gifts.entity.*;
import com.lunette.gifts.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserAccountRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository itemRepository;
    private final CustomerPhotoRepository photoRepository;
    private final OrderStatusHistoryRepository historyRepository;
    private final ExpenseRepository expenseRepository;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final BusinessSettingRepository settingRepository;
    private final WorkerActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserAccountRepository userRepository,
                           ProductRepository productRepository,
                           ProductVariantRepository variantRepository,
                           OrderRepository orderRepository,
                           OrderItemRepository itemRepository,
                           CustomerPhotoRepository photoRepository,
                           OrderStatusHistoryRepository historyRepository,
                           ExpenseRepository expenseRepository,
                           ReviewRepository reviewRepository,
                           CouponRepository couponRepository,
                           BusinessSettingRepository settingRepository,
                           WorkerActivityRepository activityRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.photoRepository = photoRepository;
        this.historyRepository = historyRepository;
        this.expenseRepository = expenseRepository;
        this.reviewRepository = reviewRepository;
        this.couponRepository = couponRepository;
        this.settingRepository = settingRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedSettings();
        seedProducts();
        seedCoupons();
        seedExpenses();
        seedReviews();
        seedSampleOrders();
    }

    private void seedUsers() {
        if (!userRepository.existsByUsername("admin")) {
            UserAccount admin = new UserAccount("admin", passwordEncoder.encode("admin123"), "Boutique Owner", "admin@lunettegifts.com", "9876543210", "ROLE_ADMIN");
            userRepository.save(admin);
        }

        if (!userRepository.existsByUsername("staff")) {
            UserAccount staff = new UserAccount("staff", passwordEncoder.encode("staff123"), "Staff Ananya", "staff@lunettegifts.com", "9876543211", "ROLE_WORKER");
            staff.setPermissions(Set.of("CREATE_ORDER", "VIEW_ORDERS", "UPDATE_ORDER_STATUS", "VIEW_CUSTOMER", "UPLOAD_FILES", "DOWNLOAD_ORDER_FILES", "VIEW_INVENTORY", "CREATE_CUSTOMER"));
            userRepository.save(staff);
        }

        if (!userRepository.existsByUsername("demo_user")) {
            UserAccount customer = new UserAccount("demo_user", passwordEncoder.encode("user123"), "Kavya Ramesh", "kavya@example.com", "9843210987", "ROLE_CUSTOMER");
            userRepository.save(customer);
        }
    }

    private void seedSettings() {
        if (settingRepository.count() == 0) {
            settingRepository.save(new BusinessSetting("business_name", "Lunette Gifts", "Official Brand Name"));
            settingRepository.save(new BusinessSetting("phone", "+91 98765 43210", "Contact Phone"));
            settingRepository.save(new BusinessSetting("email", "hello@lunettegifts.com", "Contact Email"));
            settingRepository.save(new BusinessSetting("instagram", "https://www.instagram.com/lunette_gifts/", "Instagram Profile"));
            settingRepository.save(new BusinessSetting("upi_id", "haring478-1@okicici", "Primary UPI ID for Scan & Pay"));
            settingRepository.save(new BusinessSetting("delivery_madurai", "70", "Flat delivery charge for Madurai zone"));
            settingRepository.save(new BusinessSetting("delivery_outside", "100", "Flat delivery charge outside Madurai"));
        }
    }

    private void seedProducts() {
        if (productRepository.count() == 0) {
            // 1. PHOTO FRAMES (Multi-variant: 5x5, 4x6, 5x7, A4)
            Product frames = new Product(
                    "photo-frames",
                    "Custom Memory Photo Frame",
                    "PHOTO_FRAMES",
                    "Handcrafted floral-finish picture frames crafted with premium wooden moulding and non-glare crystal acrylic. Tailored for your dearest memories.",
                    new BigDecimal("140.00"),
                    new BigDecimal("30.00"), // Customization +30
                    1,
                    "FIXED_PER_VARIANT",
                    "/images/frame-thumb.svg"
            );
            frames.setStockQuantity(65);
            frames.setLowStockThreshold(15);
            frames = productRepository.save(frames);

            // Frame Variants with DB-configured required photos!
            // 5x5 Frame -> ₹140, 1 photo
            variantRepository.save(new ProductVariant(frames, "5 × 5 Frame", "5 × 5 inches", new BigDecimal("140.00"), 1));
            // 4x6 Frame -> ₹150, 1 photo
            variantRepository.save(new ProductVariant(frames, "4 × 6 Frame", "4 × 6 inches", new BigDecimal("150.00"), 1));
            // 5x7 Frame -> ₹170, 2 photos
            variantRepository.save(new ProductVariant(frames, "5 × 7 Frame", "5 × 7 inches", new BigDecimal("170.00"), 2));
            // A4 Frame -> ₹250, 4 photos
            variantRepository.save(new ProductVariant(frames, "A4 Frame", "8.3 × 11.7 inches", new BigDecimal("250.00"), 4));

            // 2. PHOTO CARDS
            Product cards = new Product(
                    "photo-cards",
                    "Premium Mini Photo Cards",
                    "PHOTO_CARDS",
                    "Glossy velvet-matte finish miniature keepsake cards. Each card captures a vivid slice of your favorite memory. Minimum order: 10 cards.",
                    new BigDecimal("8.00"), // ₹8 per card
                    new BigDecimal("10.00"), // Customization +10
                    10, // Min 10 cards
                    "ONE_PER_UNIT",
                    "/images/cards-thumb.svg"
            );
            cards.setStockQuantity(250);
            cards.setLowStockThreshold(40);
            cards = productRepository.save(cards);
            variantRepository.save(new ProductVariant(cards, "Standard Mini Card", "2.5 × 3.5 inches", new BigDecimal("8.00"), 1));

            // 3. RING ALBUM
            Product ringAlbum = new Product(
                    "ring-albums",
                    "Vintage Floral Ring Album",
                    "RING_ALBUMS",
                    "A timeless metal-bound keepsake book with heavy archival photo cards. Easy to flip, cherish, and gift. Ring: ₹20, Cards: ₹8 each.",
                    new BigDecimal("8.00"),
                    new BigDecimal("20.00"),
                    10, // Min 10 cards
                    "ONE_PER_UNIT",
                    "/images/album-thumb.svg"
            );
            ringAlbum.setHasRingOption(true);
            ringAlbum.setRingPrice(new BigDecimal("20.00"));
            ringAlbum.setStockQuantity(45);
            ringAlbum.setLowStockThreshold(10);
            ringAlbum = productRepository.save(ringAlbum);
            variantRepository.save(new ProductVariant(ringAlbum, "Classic Metal Ring Binding", "Custom Dimensions", new BigDecimal("8.00"), 1));

            // 4. LED POLAROIDS
            Product ledPolaroid = new Product(
                    "led-polaroids",
                    "Fairy-Light LED Polaroid Set",
                    "LED_POLAROIDS",
                    "Glowing warm LED clip-string paired with retro border polaroid prints. Lights: ₹150, Cards: ₹7 each. Fills any bedroom with golden nostalgia.",
                    new BigDecimal("7.00"),
                    new BigDecimal("15.00"),
                    10, // Min 10 cards
                    "ONE_PER_UNIT",
                    "/images/led-thumb.svg"
            );
            ledPolaroid.setHasLightOption(true);
            ledPolaroid.setLightPrice(new BigDecimal("150.00"));
            ledPolaroid.setStockQuantity(30);
            ledPolaroid.setLowStockThreshold(10);
            ledPolaroid = productRepository.save(ledPolaroid);
            variantRepository.save(new ProductVariant(ledPolaroid, "Warm Fairy Light String + Polaroids", "3 × 4 inches per print", new BigDecimal("7.00"), 1));
        }
    }

    private void seedCoupons() {
        if (couponRepository.count() == 0) {
            couponRepository.save(new Coupon("WELCOME10", "PERCENTAGE", new BigDecimal("10.00"), new BigDecimal("200.00"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(6), 500));
            couponRepository.save(new Coupon("LUNETTE50", "FIXED", new BigDecimal("50.00"), new BigDecimal("500.00"), LocalDate.now().minusDays(10), LocalDate.now().plusMonths(6), 200));
        }
    }

    private void seedExpenses() {
        if (expenseRepository.count() == 0) {
            expenseRepository.save(new Expense("MATERIAL", new BigDecimal("850.00"), LocalDate.now().minusDays(3), "Imported wooden moulding & acrylic sheets", "Purchased from Madurai Arts supplier", "admin"));
            expenseRepository.save(new Expense("PACKAGING", new BigDecimal("320.00"), LocalDate.now().minusDays(2), "Bubblewrap rolls and floral blush boxes", "Bulk order for gift packaging", "admin"));
            expenseRepository.save(new Expense("ELECTRICITY", new BigDecimal("450.00"), LocalDate.now().minusDays(1), "Studio workshop utility power bill", "Monthly share for printing machines", "admin"));
        }
    }

    private void seedReviews() {
        if (reviewRepository.count() == 0) {
            Product p = productRepository.findBySlug("photo-frames").orElse(null);
            if (p != null) {
                reviewRepository.save(new Review(p, "Meera Sundaram", 5, "The 5x7 frame looks so breathtaking on our wall! The custom floral engraving was done to absolute perfection.", null, true));
                reviewRepository.save(new Review(p, "Rohit Verma", 5, "Ordered the LED Polaroid set for our 1st anniversary. My wife was in tears of joy. Exceptional packaging and quality.", null, true));
                reviewRepository.save(new Review(p, "Priya Dharshini", 5, "Fast delivery in Madurai and the photo clarity on the cards was stunning. Will definitely buy again!", null, true));
            }
        }
    }

    private void seedSampleOrders() {
        if (orderRepository.count() == 0) {
            Product frame = productRepository.findBySlug("photo-frames").orElse(null);
            Product cards = productRepository.findBySlug("photo-cards").orElse(null);
            UserAccount customer = userRepository.findByUsername("demo_user").orElse(null);

            if (frame != null && cards != null) {
                // Seed Order 1: Verified and Ready
                Order o1 = new Order();
                o1.setOrderNumber("LG-10024");
                o1.setCustomer(customer);
                o1.setCustomerName("Kavya Ramesh");
                o1.setCustomerPhone("9843210987");
                o1.setCustomerEmail("kavya@example.com");
                o1.setShippingAddress("14/B, West Masi Street, Near Temple");
                o1.setPinCode("625001");
                o1.setCity("Madurai");
                o1.setDistrict("Madurai District");
                o1.setState("Tamil Nadu");
                o1.setDeliveryZone("MADURAI");
                o1.setDeliveryCharge(new BigDecimal("70.00")); // Madurai delivery
                o1.setSubtotal(new BigDecimal("280.00"));
                o1.setDiscount(BigDecimal.ZERO);
                o1.setTotalAmount(new BigDecimal("350.00"));
                o1.setPaymentStatus("CONFIRMED");
                o1.setPaymentReference("UPI-ICICI-839210928");
                o1.setOrderStatus("IN_PRODUCTION");
                o1.setOrderNotes("Please pack with rose satin ribbon");
                o1.setCreatedAt(LocalDateTime.now().minusHours(12));
                o1 = orderRepository.save(o1);

                OrderItem item1 = new OrderItem();
                item1.setOrder(o1);
                item1.setProduct(frame);
                item1.setProductName("Custom Memory Photo Frame");
                item1.setVariantName("5 × 7 Frame");
                item1.setVariantDimensions("5 × 7 inches");
                item1.setQuantity(1);
                item1.setUnitPrice(new BigDecimal("170.00"));
                item1.setCustomizationSelected(true);
                item1.setCustomizationCharge(new BigDecimal("30.00"));
                item1.setCustomizationInstructions("Add text 'Together Always' at bottom center");
                item1.setRequiredPhotos(2);
                item1.setUploadedPhotos(2);
                item1.setItemSubtotal(new BigDecimal("200.00"));
                itemRepository.save(item1);

                photoRepository.save(new CustomerPhoto(item1, "/images/sample-couple.svg", "memory_01.jpg", "image/jpeg", 102400, "PRINT_PHOTO", 1));
                photoRepository.save(new CustomerPhoto(item1, "/images/sample-beach.svg", "memory_02.jpg", "image/jpeg", 102400, "PRINT_PHOTO", 2));

                historyRepository.save(new OrderStatusHistory(o1, null, "ORDER_PLACED", "kavya", "Placed via website"));
                historyRepository.save(new OrderStatusHistory(o1, "ORDER_PLACED", "PAYMENT_CONFIRMED", "admin", "UPI Verified"));
                historyRepository.save(new OrderStatusHistory(o1, "PAYMENT_CONFIRMED", "IN_PRODUCTION", "staff", "Crafting frame moulding"));

                // Seed Order 2: Walk-in Phone Order created by Staff
                Order o2 = new Order();
                o2.setOrderNumber("LG-10025");
                o2.setCustomer(customer);
                o2.setCustomerName("Senthil Kumar");
                o2.setCustomerPhone("9789012345");
                o2.setCustomerEmail("senthil@gmail.com");
                o2.setShippingAddress("Plot 23, Anna Nagar 2nd Street");
                o2.setPinCode("625020");
                o2.setCity("Madurai");
                o2.setDistrict("Madurai District");
                o2.setState("Tamil Nadu");
                o2.setDeliveryZone("MADURAI");
                o2.setDeliveryCharge(new BigDecimal("70.00"));
                o2.setSubtotal(new BigDecimal("160.00"));
                o2.setDiscount(BigDecimal.ZERO);
                o2.setTotalAmount(new BigDecimal("230.00"));
                o2.setPaymentStatus("PENDING");
                o2.setOrderStatus("ORDER_PLACED");
                o2.setStaffAssisted(true);
                o2.setCreatedByStaffUsername("staff");
                o2.setCreatedAt(LocalDateTime.now().minusHours(2));
                o2 = orderRepository.save(o2);

                activityRepository.save(new WorkerActivity("staff", "Staff Ananya", "ORDER_CREATED", "LG-10025", o2.getId(), "Staff assisted order entry for walk-in customer", "127.0.0.1"));
            }
        }
    }
}
