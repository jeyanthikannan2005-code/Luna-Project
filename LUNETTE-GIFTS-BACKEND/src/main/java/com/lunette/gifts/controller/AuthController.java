package com.lunette.gifts.controller;

import com.lunette.gifts.dto.AuthDto;
import com.lunette.gifts.entity.UserAccount;
import com.lunette.gifts.repository.UserAccountRepository;
import com.lunette.gifts.security.JwtTokenProvider;
import com.lunette.gifts.service.WorkerService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final WorkerService workerService;

    public AuthController(UserAccountRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtTokenProvider tokenProvider,
                          WorkerService workerService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.workerService = workerService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthDto.LoginRequest req, HttpServletRequest request) {
        Optional<UserAccount> userOpt = userRepository.findByUsername(req.getUsername().trim());
        if (userOpt.isEmpty() || !passwordEncoder.matches(req.getPassword(), userOpt.get().getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
        }

        UserAccount user = userOpt.get();
        if (!user.isActive()) {
            return ResponseEntity.status(403).body(Map.of("error", "Account has been deactivated. Please contact admin."));
        }

        user.setLastLoginAt(LocalDateTime.now());
        user.setLastActiveAt(LocalDateTime.now());
        userRepository.save(user);

        // Record worker login activity
        if ("ROLE_WORKER".equals(user.getRole())) {
            String ip = request.getRemoteAddr();
            workerService.logActivity(user.getUsername(), "LOGIN", null, null, "Worker logged into system", ip);
        }

        String token = tokenProvider.generateToken(user.getUsername(), user.getRole(), user.getPermissions());

        return ResponseEntity.ok(new AuthDto.AuthResponse(
                token,
                user.getUsername(),
                user.getFullName(),
                user.getRole(),
                user.getPermissions()
        ));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthDto.RegisterRequest req) {
        if (userRepository.existsByUsername(req.getUsername().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
        }
        if (req.getEmail() != null && !req.getEmail().isBlank() && userRepository.existsByEmail(req.getEmail().trim())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        UserAccount user = new UserAccount();
        user.setUsername(req.getUsername().trim());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setFullName(req.getFullName());
        user.setEmail(req.getEmail() != null ? req.getEmail().trim() : null);
        user.setPhone(req.getPhone());
        user.setRole("ROLE_CUSTOMER");
        user.setActive(true);

        UserAccount saved = userRepository.save(user);
        String token = tokenProvider.generateToken(saved.getUsername(), saved.getRole(), saved.getPermissions());

        return ResponseEntity.ok(new AuthDto.AuthResponse(
                token,
                saved.getUsername(),
                saved.getFullName(),
                saved.getRole(),
                saved.getPermissions()
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        UserAccount user = (UserAccount) auth.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "fullName", user.getFullName() != null ? user.getFullName() : user.getUsername(),
                "role", user.getRole(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "phone", user.getPhone() != null ? user.getPhone() : "",
                "permissions", user.getPermissions()
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(Authentication auth, HttpServletRequest request) {
        if (auth != null && auth.getPrincipal() instanceof UserAccount user) {
            if ("ROLE_WORKER".equals(user.getRole())) {
                workerService.logActivity(user.getUsername(), "LOGOUT", null, null, "Worker logged out", request.getRemoteAddr());
            }
        }
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }
}
