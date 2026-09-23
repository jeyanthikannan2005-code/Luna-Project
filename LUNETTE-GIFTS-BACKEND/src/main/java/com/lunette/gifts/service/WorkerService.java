package com.lunette.gifts.service;

import com.lunette.gifts.dto.AdminDto;
import com.lunette.gifts.entity.UserAccount;
import com.lunette.gifts.entity.WorkerActivity;
import com.lunette.gifts.repository.UserAccountRepository;
import com.lunette.gifts.repository.WorkerActivityRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class WorkerService {

    private final UserAccountRepository userRepository;
    private final WorkerActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    public WorkerService(UserAccountRepository userRepository,
                         WorkerActivityRepository activityRepository,
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserAccount> getAllWorkers() {
        return userRepository.findByRoleOrderByCreatedAtDesc("ROLE_WORKER");
    }

    public UserAccount createWorker(AdminDto.WorkerCreateRequest req) {
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + req.getUsername());
        }

        UserAccount worker = new UserAccount();
        worker.setUsername(req.getUsername());
        worker.setPassword(passwordEncoder.encode(req.getPassword()));
        worker.setFullName(req.getFullName());
        worker.setPhone(req.getPhone());
        worker.setEmail(req.getEmail());
        worker.setRole("ROLE_WORKER");
        worker.setActive(true);

        Set<String> perms = req.getPermissions();
        if (perms == null || perms.isEmpty()) {
            perms = Set.of("CREATE_ORDER", "VIEW_ORDERS", "UPDATE_ORDER_STATUS", "UPLOAD_FILES");
        }
        worker.setPermissions(perms);

        return userRepository.save(worker);
    }

    public UserAccount updateWorker(Long id, AdminDto.WorkerUpdateRequest req) {
        UserAccount worker = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Worker not found: " + id));

        if (req.getFullName() != null) worker.setFullName(req.getFullName());
        if (req.getPhone() != null) worker.setPhone(req.getPhone());
        if (req.getEmail() != null) worker.setEmail(req.getEmail());
        worker.setActive(req.isActive());
        if (req.getPermissions() != null) {
            worker.setPermissions(req.getPermissions());
        }

        return userRepository.save(worker);
    }

    /**
     * Append-only activity logging.
     * Records worker activity with timestamp and cannot be deleted by workers.
     */
    public void logActivity(String workerUsername, String activityType, String orderNumber, Long orderId, String details, String ipAddress) {
        String fullName = workerUsername;
        UserAccount user = userRepository.findByUsername(workerUsername).orElse(null);
        if (user != null) {
            if (user.getFullName() != null) fullName = user.getFullName();
            user.setLastActiveAt(LocalDateTime.now());
            userRepository.save(user);
        }

        WorkerActivity activity = new WorkerActivity(workerUsername, fullName, activityType, orderNumber, orderId, details, ipAddress);
        activityRepository.save(activity);
    }

    public List<WorkerActivity> getActivitiesByFilter(String filter, LocalDate startDate, LocalDate endDate) {
        LocalDateTime now = LocalDateTime.now();
        if ("TODAY".equalsIgnoreCase(filter)) {
            LocalDateTime start = LocalDate.now().atStartOfDay();
            return activityRepository.findByTimestampBetweenOrderByTimestampDesc(start, now);
        } else if ("YESTERDAY".equalsIgnoreCase(filter)) {
            LocalDateTime start = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime end = LocalDate.now().atStartOfDay();
            return activityRepository.findByTimestampBetweenOrderByTimestampDesc(start, end);
        } else if ("THIS_WEEK".equalsIgnoreCase(filter)) {
            LocalDateTime start = LocalDate.now().minusDays(7).atStartOfDay();
            return activityRepository.findByTimestampBetweenOrderByTimestampDesc(start, now);
        } else if ("THIS_MONTH".equalsIgnoreCase(filter)) {
            LocalDateTime start = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            return activityRepository.findByTimestampBetweenOrderByTimestampDesc(start, now);
        } else if ("CUSTOM".equalsIgnoreCase(filter) && startDate != null && endDate != null) {
            return activityRepository.findByTimestampBetweenOrderByTimestampDesc(
                    startDate.atStartOfDay(),
                    endDate.plusDays(1).atStartOfDay()
            );
        }
        return activityRepository.findAllByOrderByTimestampDesc();
    }

    public List<AdminDto.WorkerPerformance> getWorkerPerformance() {
        List<UserAccount> workers = getAllWorkers();
        List<AdminDto.WorkerPerformance> list = new ArrayList<>();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");

        for (UserAccount w : workers) {
            AdminDto.WorkerPerformance p = new AdminDto.WorkerPerformance();
            p.setUsername(w.getUsername());
            p.setFullName(w.getFullName() != null ? w.getFullName() : w.getUsername());
            p.setActive(w.isActive());

            // Active within last 30 minutes considered online
            boolean isOnline = w.getLastActiveAt() != null && w.getLastActiveAt().isAfter(LocalDateTime.now().minusMinutes(30));
            p.setOnline(isOnline);

            long created = activityRepository.countByWorkerUsernameAndActivityType(w.getUsername(), "ORDER_CREATED");
            long updated = activityRepository.countByWorkerUsernameAndActivityType(w.getUsername(), "ORDER_STATUS_UPDATED");
            long downloaded = activityRepository.countByWorkerUsernameAndActivityType(w.getUsername(), "ORDER_FILES_DOWNLOADED");

            p.setOrdersCreated(created);
            p.setOrdersUpdated(updated);
            p.setFilesDownloaded(downloaded);

            p.setLastActive(w.getLastActiveAt() != null ? w.getLastActiveAt().format(dtf) : "Never");
            list.add(p);
        }
        return list;
    }
}
