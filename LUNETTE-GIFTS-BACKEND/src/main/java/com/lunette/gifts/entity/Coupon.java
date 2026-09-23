package com.lunette.gifts.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 30)
    private String code;

    // PERCENTAGE or FIXED
    @Column(nullable = false, length = 20)
    private String discountType = "PERCENTAGE";

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 10, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    private LocalDate validFrom;
    private LocalDate validTo;

    private int usageLimit = 100;
    private int usageCount = 0;

    private boolean active = true;

    public Coupon() {}

    public Coupon(String code, String discountType, BigDecimal discountValue, BigDecimal minOrderAmount, LocalDate validFrom, LocalDate validTo, int usageLimit) {
        this.code = code;
        this.discountType = discountType;
        this.discountValue = discountValue;
        this.minOrderAmount = minOrderAmount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.usageLimit = usageLimit;
        this.usageCount = 0;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDiscountType() { return discountType; }
    public void setDiscountType(String discountType) { this.discountType = discountType; }

    public BigDecimal getDiscountValue() { return discountValue; }
    public void setDiscountValue(BigDecimal discountValue) { this.discountValue = discountValue; }

    public BigDecimal getMinOrderAmount() { return minOrderAmount; }
    public void setMinOrderAmount(BigDecimal minOrderAmount) { this.minOrderAmount = minOrderAmount; }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }

    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }

    public int getUsageLimit() { return usageLimit; }
    public void setUsageLimit(int usageLimit) { this.usageLimit = usageLimit; }

    public int getUsageCount() { return usageCount; }
    public void setUsageCount(int usageCount) { this.usageCount = usageCount; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public boolean isValid(BigDecimal orderSubtotal) {
        if (!active) return false;
        if (usageCount >= usageLimit) return false;
        LocalDate today = LocalDate.now();
        if (validFrom != null && today.isBefore(validFrom)) return false;
        if (validTo != null && today.isAfter(validTo)) return false;
        if (minOrderAmount != null && orderSubtotal.compareTo(minOrderAmount) < 0) return false;
        return true;
    }
}
