package com.lunette.gifts.controller;

import com.lunette.gifts.entity.Order;
import com.lunette.gifts.entity.UserAccount;
import com.lunette.gifts.repository.UserAccountRepository;
import com.lunette.gifts.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final OrderService orderService;
    private final UserAccountRepository userRepository;

    public CustomerController(OrderService orderService, UserAccountRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getMyOrders(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        List<Order> orders = orderService.getOrdersByCustomerUsername(user.getUsername());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<?> getMyOrderDetails(@PathVariable String orderNumber, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        Optional<Order> orderOpt = orderService.getOrderByNumber(orderNumber.trim().toUpperCase());
        if (orderOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order o = orderOpt.get();
        // Ensure customer can only view their own order (or match phone/email)
        boolean isOwner = (o.getCustomer() != null && o.getCustomer().getId().equals(user.getId()))
                || (o.getCustomerEmail() != null && o.getCustomerEmail().equalsIgnoreCase(user.getEmail()))
                || (o.getCustomerPhone() != null && o.getCustomerPhone().equalsIgnoreCase(user.getPhone()));

        if (!isOwner && !"ROLE_ADMIN".equals(user.getRole()) && !"ROLE_WORKER".equals(user.getRole())) {
            return ResponseEntity.status(403).body(Map.of("error", "Access denied to this order"));
        }

        return ResponseEntity.ok(o);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        return ResponseEntity.ok(user);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> payload, Authentication auth) {
        if (auth == null || !(auth.getPrincipal() instanceof UserAccount user)) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        UserAccount fresh = userRepository.findById(user.getId()).orElse(user);
        if (payload.containsKey("fullName")) fresh.setFullName(payload.get("fullName"));
        if (payload.containsKey("phone")) fresh.setPhone(payload.get("phone"));
        if (payload.containsKey("email")) fresh.setEmail(payload.get("email"));
        userRepository.save(fresh);

        return ResponseEntity.ok(Map.of("message", "Profile updated successfully", "user", fresh));
    }
}
