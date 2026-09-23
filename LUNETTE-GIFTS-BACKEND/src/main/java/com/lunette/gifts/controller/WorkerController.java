package com.lunette.gifts.controller;

import com.lunette.gifts.dto.OrderDto;
import com.lunette.gifts.entity.Order;
import com.lunette.gifts.entity.Product;
import com.lunette.gifts.entity.UserAccount;
import com.lunette.gifts.repository.UserAccountRepository;
import com.lunette.gifts.service.FileStorageService;
import com.lunette.gifts.service.InventoryService;
import com.lunette.gifts.service.OrderService;
import com.lunette.gifts.service.ProductService;
import com.lunette.gifts.service.WorkerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/worker")
public class WorkerController {

    private final OrderService orderService;
    private final ProductService productService;
    private final InventoryService inventoryService;
    private final FileStorageService fileStorageService;
    private final WorkerService workerService;
    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkerController(OrderService orderService,
                            ProductService productService,
                            InventoryService inventoryService,
                            FileStorageService fileStorageService,
                            WorkerService workerService,
                            UserAccountRepository userRepository,
                            PasswordEncoder passwordEncoder) {
        this.orderService = orderService;
        this.productService = productService;
        this.inventoryService = inventoryService;
        this.fileStorageService = fileStorageService;
        this.workerService = workerService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    private UserAccount getWorker(Authentication auth) {
        if (auth != null && auth.getPrincipal() instanceof UserAccount user) {
            return user;
        }
        throw new IllegalStateException("Unauthenticated worker access");
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getOrders(Authentication auth) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("VIEW_ORDERS")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission VIEW_ORDERS required"));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/search")
    public ResponseEntity<?> searchOrders(@RequestParam("query") String query, Authentication auth) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("VIEW_ORDERS")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission VIEW_ORDERS required"));
        }
        return ResponseEntity.ok(orderService.searchOrders(query));
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createStaffOrder(@RequestBody OrderDto.OrderCreateRequest req, Authentication auth, HttpServletRequest request) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("CREATE_ORDER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission CREATE_ORDER required"));
        }

        try {
            Order order = orderService.createOrder(req, worker.getUsername(), true);
            workerService.logActivity(worker.getUsername(), "ORDER_CREATED", order.getOrderNumber(), order.getId(),
                    "Order created for customer: " + order.getCustomerName() + " (₹" + order.getTotalAmount() + ")", request.getRemoteAddr());

            return ResponseEntity.ok(Map.of(
                    "orderNumber", order.getOrderNumber(),
                    "orderId", order.getId(),
                    "totalAmount", order.getTotalAmount(),
                    "status", order.getOrderStatus(),
                    "message", "Order created successfully by staff."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id,
                                         @RequestBody OrderDto.OrderStatusUpdateRequest req,
                                         Authentication auth,
                                         HttpServletRequest request) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("UPDATE_ORDER_STATUS")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission UPDATE_ORDER_STATUS required"));
        }

        try {
            Order updated = orderService.updateOrderStatus(id, req.getStatus(), req.getNotes(), worker.getUsername());
            workerService.logActivity(worker.getUsername(), "ORDER_STATUS_UPDATED", updated.getOrderNumber(), updated.getId(),
                    "Status updated to: " + req.getStatus(), request.getRemoteAddr());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file,
                                         @RequestParam(value = "category", defaultValue = "staff_orders") String category,
                                         Authentication auth) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("UPLOAD_FILES")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission UPLOAD_FILES required"));
        }

        try {
            return ResponseEntity.ok(fileStorageService.storeFile(file, category));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/orders/{id}/download-zip")
    public ResponseEntity<?> downloadOrderFiles(@PathVariable Long id, Authentication auth, HttpServletRequest request) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("DOWNLOAD_ORDER_FILES")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission DOWNLOAD_ORDER_FILES required"));
        }

        try {
            Order order = orderService.getOrderById(id).orElseThrow(() -> new IllegalArgumentException("Order not found"));
            byte[] zipData = fileStorageService.createOrderZip(id);

            workerService.logActivity(worker.getUsername(), "ORDER_FILES_DOWNLOADED", order.getOrderNumber(), order.getId(),
                    "Staff downloaded photo bundle", request.getRemoteAddr());

            String filename = "ORDER_" + order.getOrderNumber() + "_FILES.zip";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(zipData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/inventory")
    public ResponseEntity<?> getInventory(Authentication auth) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("VIEW_INVENTORY")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission VIEW_INVENTORY required"));
        }
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/customers/lookup")
    public ResponseEntity<?> lookupCustomer(@RequestParam("query") String query, Authentication auth) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("VIEW_CUSTOMER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission VIEW_CUSTOMER required"));
        }

        Optional<UserAccount> userByPhone = userRepository.findByPhone(query.trim());
        if (userByPhone.isPresent()) {
            UserAccount u = userByPhone.get();
            return ResponseEntity.ok(Map.of(
                    "id", u.getId(),
                    "fullName", u.getFullName() != null ? u.getFullName() : "",
                    "phone", u.getPhone() != null ? u.getPhone() : "",
                    "email", u.getEmail() != null ? u.getEmail() : ""
            ));
        }

        Optional<UserAccount> userByEmail = userRepository.findByEmail(query.trim());
        if (userByEmail.isPresent()) {
            UserAccount u = userByEmail.get();
            return ResponseEntity.ok(Map.of(
                    "id", u.getId(),
                    "fullName", u.getFullName() != null ? u.getFullName() : "",
                    "phone", u.getPhone() != null ? u.getPhone() : "",
                    "email", u.getEmail() != null ? u.getEmail() : ""
            ));
        }

        return ResponseEntity.status(404).body(Map.of("message", "Customer not found"));
    }

    @PostMapping("/customers")
    public ResponseEntity<?> createCustomer(@RequestBody Map<String, String> payload, Authentication auth, HttpServletRequest request) {
        UserAccount worker = getWorker(auth);
        if (!worker.hasPermission("CREATE_CUSTOMER")) {
            return ResponseEntity.status(403).body(Map.of("error", "Permission CREATE_CUSTOMER required"));
        }

        String phone = payload.get("phone");
        String fullName = payload.get("fullName");
        String email = payload.get("email");

        if (phone == null || phone.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Phone number is required"));
        }

        String username = "cust_" + phone.replaceAll("[^0-9]", "");
        if (userRepository.existsByUsername(username)) {
            UserAccount existing = userRepository.findByUsername(username).get();
            return ResponseEntity.ok(existing);
        }

        UserAccount newCust = new UserAccount(username, passwordEncoder.encode("Welcome@123"), fullName, email, phone, "ROLE_CUSTOMER");
        UserAccount saved = userRepository.save(newCust);

        workerService.logActivity(worker.getUsername(), "CUSTOMER_CREATED", null, null,
                "New customer registered: " + fullName + " (" + phone + ")", request.getRemoteAddr());

        return ResponseEntity.ok(saved);
    }
}
