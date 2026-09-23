package com.lunette.gifts.controller;

import com.lunette.gifts.dto.AdminDto;
import com.lunette.gifts.dto.OrderDto;
import com.lunette.gifts.entity.*;
import com.lunette.gifts.repository.*;
import com.lunette.gifts.service.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AnalyticsService analyticsService;
    private final OrderService orderService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final ExpenseService expenseService;
    private final WorkerService workerService;
    private final ReportService reportService;
    private final FileStorageService fileStorageService;
    private final ReviewRepository reviewRepository;
    private final CouponRepository couponRepository;
    private final BusinessSettingRepository settingRepository;
    private final UserAccountRepository userRepository;

    public AdminController(AnalyticsService analyticsService,
                           OrderService orderService,
                           ProductService productService,
                           InventoryService inventoryService,
                           ExpenseService expenseService,
                           WorkerService workerService,
                           ReportService reportService,
                           FileStorageService fileStorageService,
                           ReviewRepository reviewRepository,
                           CouponRepository couponRepository,
                           BusinessSettingRepository settingRepository,
                           UserAccountRepository userRepository) {
        this.analyticsService = analyticsService;
        this.orderService = orderService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.expenseService = expenseService;
        this.workerService = workerService;
        this.reportService = reportService;
        this.fileStorageService = fileStorageService;
        this.reviewRepository = reviewRepository;
        this.couponRepository = couponRepository;
        this.settingRepository = settingRepository;
        this.userRepository = userRepository;
    }

    private String getUsername(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserAccount user) {
            return user.getUsername();
        }
        return "Admin";
    }

    // 1. DASHBOARD & INSIGHTS
    @GetMapping("/dashboard")
    public ResponseEntity<AdminDto.DashboardSummary> getDashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/insights")
    public ResponseEntity<List<AdminDto.SmartSuggestion>> getInsights() {
        return ResponseEntity.ok(analyticsService.getSmartBusinessSuggestions());
    }

    @GetMapping("/predictions")
    public ResponseEntity<List<String>> getPredictions() {
        return ResponseEntity.ok(analyticsService.getPredictions());
    }

    // 2. ORDER MANAGEMENT
    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getOrders(@RequestParam(value = "search", required = false) String search) {
        return ResponseEntity.ok(orderService.searchOrders(search));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderDetails(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateOrderStatus(@PathVariable Long id,
                                               @RequestBody OrderDto.OrderStatusUpdateRequest req,
                                               Authentication auth) {
        try {
            Order updated = orderService.updateOrderStatus(id, req.getStatus(), req.getNotes(), getUsername(auth));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/orders/{id}/verify-payment")
    public ResponseEntity<?> verifyPayment(@PathVariable Long id,
                                           @RequestBody OrderDto.PaymentVerificationRequest req,
                                           Authentication auth) {
        try {
            Order updated = orderService.verifyPayment(id, req.getPaymentStatus(), req.getNotes(), getUsername(auth));
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}/download-zip")
    public ResponseEntity<?> downloadOrderFiles(@PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found with id: " + id));

            byte[] zipBytes = fileStorageService.createOrderZip(id);
            String filename = "ORDER_" + order.getOrderNumber() + "_FILES.zip";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipBytes);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 3. PRODUCTS & FRAME CONFIGURATION
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllActiveProducts());
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody AdminDto.ProductUpdateRequest req) {
        try {
            Product updated = productService.updateProduct(id, req);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/variants/{variantId}")
    public ResponseEntity<?> updateVariant(@PathVariable Long variantId, @RequestBody AdminDto.VariantUpdateRequest req) {
        try {
            ProductVariant updated = productService.updateVariant(variantId, req);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 4. INVENTORY MANAGEMENT
    @GetMapping("/inventory")
    public ResponseEntity<List<Product>> getInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @PostMapping("/inventory/{id}/adjust")
    public ResponseEntity<?> adjustInventory(@PathVariable Long id,
                                             @RequestBody AdminDto.InventoryAdjustmentRequest req,
                                             Authentication auth) {
        try {
            Product product = inventoryService.adjustStock(id, req, getUsername(auth));
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/inventory/transactions")
    public ResponseEntity<List<InventoryTransaction>> getInventoryTransactions() {
        return ResponseEntity.ok(inventoryService.getTransactions());
    }

    // 5. WORKER MANAGEMENT & MONITORING
    @GetMapping("/workers")
    public ResponseEntity<List<UserAccount>> getWorkers() {
        return ResponseEntity.ok(workerService.getAllWorkers());
    }

    @PostMapping("/workers")
    public ResponseEntity<?> createWorker(@RequestBody AdminDto.WorkerCreateRequest req) {
        try {
            UserAccount created = workerService.createWorker(req);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/workers/{id}")
    public ResponseEntity<?> updateWorker(@PathVariable Long id, @RequestBody AdminDto.WorkerUpdateRequest req) {
        try {
            UserAccount updated = workerService.updateWorker(id, req);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/workers/activities")
    public ResponseEntity<List<WorkerActivity>> getWorkerActivities(
            @RequestParam(value = "filter", defaultValue = "ALL") String filter,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(workerService.getActivitiesByFilter(filter, startDate, endDate));
    }

    @GetMapping("/workers/performance")
    public ResponseEntity<List<AdminDto.WorkerPerformance>> getWorkerPerformance() {
        return ResponseEntity.ok(workerService.getWorkerPerformance());
    }

    // 6. EXPENSE MANAGEMENT
    @GetMapping("/expenses")
    public ResponseEntity<List<Expense>> getExpenses() {
        return ResponseEntity.ok(expenseService.getAllExpenses());
    }

    @PostMapping("/expenses")
    public ResponseEntity<?> addExpense(@RequestBody AdminDto.ExpenseRequest req, Authentication auth) {
        try {
            Expense saved = expenseService.addExpense(req, getUsername(auth));
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/expenses/{id}")
    public ResponseEntity<?> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok(Map.of("message", "Expense deleted"));
    }

    // 7. CUSTOMER MANAGEMENT
    @GetMapping("/customers")
    public ResponseEntity<?> getCustomers() {
        List<UserAccount> customers = userRepository.findByRoleOrderByCreatedAtDesc("ROLE_CUSTOMER");
        List<Map<String, Object>> result = new ArrayList<>();

        for (UserAccount c : customers) {
            List<Order> orders = orderService.getOrdersByCustomerUsername(c.getUsername());
            BigDecimal totalSpent = orders.stream()
                    .filter(o -> "CONFIRMED".equalsIgnoreCase(o.getPaymentStatus()))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("username", c.getUsername());
            map.put("fullName", c.getFullName() != null ? c.getFullName() : c.getUsername());
            map.put("email", c.getEmail() != null ? c.getEmail() : "N/A");
            map.put("phone", c.getPhone() != null ? c.getPhone() : "N/A");
            map.put("orderCount", orders.size());
            map.put("totalSpent", totalSpent);
            map.put("joinedDate", c.getCreatedAt());
            map.put("lastOrder", orders.isEmpty() ? "None" : orders.get(0).getOrderNumber());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    // 8. REVIEWS MODERATION
    @GetMapping("/reviews")
    public ResponseEntity<List<Review>> getReviews() {
        return ResponseEntity.ok(reviewRepository.findAllByOrderByCreatedAtDesc());
    }

    @PatchMapping("/reviews/{id}/toggle-approval")
    public ResponseEntity<?> toggleReviewApproval(@PathVariable Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        review.setApproved(!review.isApproved());
        return ResponseEntity.ok(reviewRepository.save(review));
    }

    // 9. COUPONS & OFFERS
    @GetMapping("/coupons")
    public ResponseEntity<List<Coupon>> getCoupons() {
        return ResponseEntity.ok(couponRepository.findAll());
    }

    @PostMapping("/coupons")
    public ResponseEntity<?> createCoupon(@RequestBody Coupon coupon) {
        try {
            Coupon saved = couponRepository.save(coupon);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/coupons/{id}/toggle")
    public ResponseEntity<?> toggleCoupon(@PathVariable Long id) {
        Coupon c = couponRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Coupon not found"));
        c.setActive(!c.isActive());
        return ResponseEntity.ok(couponRepository.save(c));
    }

    // 10. BUSINESS REPORTS (CSV EXPORT)
    @GetMapping("/reports/orders/csv")
    public ResponseEntity<byte[]> exportOrdersCsv() {
        byte[] csv = reportService.generateOrdersCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Orders_Report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/reports/inventory/csv")
    public ResponseEntity<byte[]> exportInventoryCsv() {
        byte[] csv = reportService.generateInventoryCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Inventory_Report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/reports/expenses/csv")
    public ResponseEntity<byte[]> exportExpensesCsv() {
        byte[] csv = reportService.generateExpensesCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Expenses_Report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    @GetMapping("/reports/worker-activities/csv")
    public ResponseEntity<byte[]> exportWorkerActivityCsv() {
        byte[] csv = reportService.generateWorkerActivityCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Worker_Activity_Report.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv);
    }

    // 11. BUSINESS SETTINGS
    @GetMapping("/settings")
    public ResponseEntity<List<BusinessSetting>> getSettings() {
        return ResponseEntity.ok(settingRepository.findAll());
    }

    @PostMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, String> settings) {
        for (Map.Entry<String, String> entry : settings.entrySet()) {
            BusinessSetting bs = settingRepository.findById(entry.getKey())
                    .orElse(new BusinessSetting(entry.getKey(), entry.getValue(), ""));
            bs.setSettingValue(entry.getValue());
            settingRepository.save(bs);
        }
        return ResponseEntity.ok(Map.of("message", "Settings updated successfully"));
    }
}
