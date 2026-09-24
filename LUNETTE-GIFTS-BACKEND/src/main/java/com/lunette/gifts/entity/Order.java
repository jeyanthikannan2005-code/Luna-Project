package com.lunette.gifts.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String orderNumber; // e.g. LG-10024

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_user_id")
    private UserAccount customer;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String customerPhone;

    @Column(length = 100)
    private String customerEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(nullable = false, length = 10)
    private String pinCode;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String district;

    @Column(length = 100)
    private String state;

    @Column(nullable = false, length = 50)
    private String deliveryZone; // MADURAI (70) or OUTSIDE_MADURAI (100)

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryCharge; // Charged once per order

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 10, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(length = 30)
    private String couponCode;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(nullable = false, length = 30)
    private String paymentStatus = "PENDING"; // PENDING, CONFIRMED, REJECTED

    @Column(length = 500)
    private String paymentProofUrl;

    @Column(length = 100)
    private String paymentReference;

    @Column(nullable = false, length = 30)
    private String orderStatus = "ORDER_PLACED";
    // ORDER_PLACED, PAYMENT_PENDING, PAYMENT_CONFIRMED, DESIGN_PENDING, DESIGN_APPROVED,
    // IN_PRODUCTION, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED, REFUND_REQUESTED, REFUNDED

    @Column(columnDefinition = "TEXT")
    private String orderNotes;

    // Staff assisted flag (if created by staff order entry)
    private boolean staffAssisted = false;
    private String createdByStaffUsername;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Order() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }

    public UserAccount getCustomer() { return customer; }
    public void setCustomer(UserAccount customer) { this.customer = customer; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getPinCode() { return pinCode; }
    public void setPinCode(String pinCode) { this.pinCode = pinCode; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getDeliveryZone() { return deliveryZone; }
    public void setDeliveryZone(String deliveryZone) { this.deliveryZone = deliveryZone; }

    public BigDecimal getDeliveryCharge() { return deliveryCharge; }
    public void setDeliveryCharge(BigDecimal deliveryCharge) { this.deliveryCharge = deliveryCharge; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public String getPaymentProofUrl() { return paymentProofUrl; }
    public void setPaymentProofUrl(String paymentProofUrl) { this.paymentProofUrl = paymentProofUrl; }

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getOrderNotes() { return orderNotes; }
    public void setOrderNotes(String orderNotes) { this.orderNotes = orderNotes; }

    public boolean isStaffAssisted() { return staffAssisted; }
    public void setStaffAssisted(boolean staffAssisted) { this.staffAssisted = staffAssisted; }

    public String getCreatedByStaffUsername() { return createdByStaffUsername; }
    public void setCreatedByStaffUsername(String createdByStaffUsername) { this.createdByStaffUsername = createdByStaffUsername; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public List<OrderStatusHistory> getStatusHistory() { return statusHistory; }
    public void setStatusHistory(List<OrderStatusHistory> statusHistory) { this.statusHistory = statusHistory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
