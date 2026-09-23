package com.lunette.gifts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "worker_activities")
public class WorkerActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String workerUsername;

    @Column(length = 100)
    private String workerFullName;

    // ORDER_CREATED, ORDER_STATUS_UPDATED, ORDER_FILES_DOWNLOADED, CUSTOMER_CREATED, LOGIN, LOGOUT, STOCK_ADJUSTED
    @Column(nullable = false, length = 50)
    private String activityType;

    @Column(length = 50)
    private String orderNumber;

    private Long orderId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 60)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public WorkerActivity() {}

    public WorkerActivity(String workerUsername, String workerFullName, String activityType, String orderNumber, Long orderId, String details, String ipAddress) {
        this.workerUsername = workerUsername;
        this.workerFullName = workerFullName;
        this.activityType = activityType;
        this.orderNumber = orderNumber;
        this.orderId = orderId;
        this.details = details;
        this.ipAddress = ipAddress;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getWorkerUsername() { return workerUsername; }
    public void setWorkerUsername(String workerUsername) { this.workerUsername = workerUsername; }

    public String getWorkerFullName() { return workerFullName; }
    public void setWorkerFullName(String workerFullName) { this.workerFullName = workerFullName; }

    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
