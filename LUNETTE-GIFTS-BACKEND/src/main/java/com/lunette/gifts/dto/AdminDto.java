package com.lunette.gifts.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public class AdminDto {

    public static class AttentionItem {
        private String type;
        private String message;
        private int count;
        private String actionUrl;

        public AttentionItem() {}
        public AttentionItem(String type, String message, int count, String actionUrl) {
            this.type = type;
            this.message = message;
            this.count = count;
            this.actionUrl = actionUrl;
        }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
        public String getActionUrl() { return actionUrl; }
        public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }
    }

    public static class MetricSnapshot {
        private BigDecimal sales = BigDecimal.ZERO;
        private long orders = 0;
        private BigDecimal expenses = BigDecimal.ZERO;
        private BigDecimal estimatedProfit = BigDecimal.ZERO;
        private long newCustomers = 0;

        public MetricSnapshot() {}

        public BigDecimal getSales() { return sales; }
        public void setSales(BigDecimal sales) { this.sales = sales; }
        public long getOrders() { return orders; }
        public void setOrders(long orders) { this.orders = orders; }
        public BigDecimal getExpenses() { return expenses; }
        public void setExpenses(BigDecimal expenses) { this.expenses = expenses; }
        public BigDecimal getEstimatedProfit() { return estimatedProfit; }
        public void setEstimatedProfit(BigDecimal estimatedProfit) { this.estimatedProfit = estimatedProfit; }
        public long getNewCustomers() { return newCustomers; }
        public void setNewCustomers(long newCustomers) { this.newCustomers = newCustomers; }
    }

    public static class DashboardSummary {
        private List<AttentionItem> attentionNeeded;
        private MetricSnapshot today;
        private MetricSnapshot month;
        private MetricSnapshot year;
        private long pendingPaymentCount;
        private long pendingDesignCount;
        private long lowStockCount;
        private long readyForDeliveryCount;

        public DashboardSummary() {}

        public List<AttentionItem> getAttentionNeeded() { return attentionNeeded; }
        public void setAttentionNeeded(List<AttentionItem> attentionNeeded) { this.attentionNeeded = attentionNeeded; }
        public MetricSnapshot getToday() { return today; }
        public void setToday(MetricSnapshot today) { this.today = today; }
        public MetricSnapshot getMonth() { return month; }
        public void setMonth(MetricSnapshot month) { this.month = month; }
        public MetricSnapshot getYear() { return year; }
        public void setYear(MetricSnapshot year) { this.year = year; }
        public long getPendingPaymentCount() { return pendingPaymentCount; }
        public void setPendingPaymentCount(long pendingPaymentCount) { this.pendingPaymentCount = pendingPaymentCount; }
        public long getPendingDesignCount() { return pendingDesignCount; }
        public void setPendingDesignCount(long pendingDesignCount) { this.pendingDesignCount = pendingDesignCount; }
        public long getLowStockCount() { return lowStockCount; }
        public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }
        public long getReadyForDeliveryCount() { return readyForDeliveryCount; }
        public void setReadyForDeliveryCount(long readyForDeliveryCount) { this.readyForDeliveryCount = readyForDeliveryCount; }
    }

    public static class VariantUpdateRequest {
        private String name;
        private String dimensions;
        private BigDecimal price;
        private int requiredPhotos; // Admin can adjust the required photo count!

        public VariantUpdateRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public int getRequiredPhotos() { return requiredPhotos; }
        public void setRequiredPhotos(int requiredPhotos) { this.requiredPhotos = requiredPhotos; }
    }

    public static class ProductUpdateRequest {
        private String name;
        private String description;
        private BigDecimal basePrice;
        private BigDecimal customizationPrice;
        private int minQuantity;
        private String photoRule;
        private BigDecimal lightPrice;
        private BigDecimal ringPrice;
        private int stockQuantity;
        private int lowStockThreshold;

        public ProductUpdateRequest() {}

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        public BigDecimal getCustomizationPrice() { return customizationPrice; }
        public void setCustomizationPrice(BigDecimal customizationPrice) { this.customizationPrice = customizationPrice; }
        public int getMinQuantity() { return minQuantity; }
        public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
        public String getPhotoRule() { return photoRule; }
        public void setPhotoRule(String photoRule) { this.photoRule = photoRule; }
        public BigDecimal getLightPrice() { return lightPrice; }
        public void setLightPrice(BigDecimal lightPrice) { this.lightPrice = lightPrice; }
        public BigDecimal getRingPrice() { return ringPrice; }
        public void setRingPrice(BigDecimal ringPrice) { this.ringPrice = ringPrice; }
        public int getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
        public int getLowStockThreshold() { return lowStockThreshold; }
        public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }
    }

    public static class InventoryAdjustmentRequest {
        private int changeQuantity;
        private String reason;

        public InventoryAdjustmentRequest() {}

        public int getChangeQuantity() { return changeQuantity; }
        public void setChangeQuantity(int changeQuantity) { this.changeQuantity = changeQuantity; }
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ExpenseRequest {
        private String category;
        private BigDecimal amount;
        private LocalDate expenseDate;
        private String description;
        private String notes;

        public ExpenseRequest() {}

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public LocalDate getExpenseDate() { return expenseDate; }
        public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    public static class WorkerCreateRequest {
        private String username;
        private String password;
        private String fullName;
        private String phone;
        private String email;
        private Set<String> permissions;

        public WorkerCreateRequest() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    }

    public static class WorkerUpdateRequest {
        private String fullName;
        private String phone;
        private String email;
        private boolean active;
        private Set<String> permissions;

        public WorkerUpdateRequest() {}

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public Set<String> getPermissions() { return permissions; }
        public void setPermissions(Set<String> permissions) { this.permissions = permissions; }
    }

    public static class WorkerPerformance {
        private String username;
        private String fullName;
        private boolean active;
        private boolean online;
        private long ordersCreated;
        private long ordersUpdated;
        private long filesDownloaded;
        private String lastActive;

        public WorkerPerformance() {}

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        public boolean isOnline() { return online; }
        public void setOnline(boolean online) { this.online = online; }
        public long getOrdersCreated() { return ordersCreated; }
        public void setOrdersCreated(long ordersCreated) { this.ordersCreated = ordersCreated; }
        public long getOrdersUpdated() { return ordersUpdated; }
        public void setOrdersUpdated(long ordersUpdated) { this.ordersUpdated = ordersUpdated; }
        public long getFilesDownloaded() { return filesDownloaded; }
        public void setFilesDownloaded(long filesDownloaded) { this.filesDownloaded = filesDownloaded; }
        public String getLastActive() { return lastActive; }
        public void setLastActive(String lastActive) { this.lastActive = lastActive; }
    }

    public static class SmartSuggestion {
        private String title;
        private String why;
        private String sourceData;
        private String suggestedAction;

        public SmartSuggestion() {}
        public SmartSuggestion(String title, String why, String sourceData, String suggestedAction) {
            this.title = title;
            this.why = why;
            this.sourceData = sourceData;
            this.suggestedAction = suggestedAction;
        }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getWhy() { return why; }
        public void setWhy(String why) { this.why = why; }
        public String getSourceData() { return sourceData; }
        public void setSourceData(String sourceData) { this.sourceData = sourceData; }
        public String getSuggestedAction() { return suggestedAction; }
        public void setSuggestedAction(String suggestedAction) { this.suggestedAction = suggestedAction; }
    }
}
