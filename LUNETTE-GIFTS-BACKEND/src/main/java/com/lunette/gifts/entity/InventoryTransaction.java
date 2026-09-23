package com.lunette.gifts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_transactions")
public class InventoryTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private int previousStock;
    private int changeQuantity;
    private int newStock;

    @Column(nullable = false, length = 200)
    private String reason; // e.g. "Restocked 50 units", "Order LG-10024 fulfilled", "Manual audit adjustment"

    @Column(length = 60)
    private String updatedBy; // username

    private LocalDateTime timestamp = LocalDateTime.now();

    public InventoryTransaction() {}

    public InventoryTransaction(Product product, int previousStock, int changeQuantity, int newStock, String reason, String updatedBy) {
        this.product = product;
        this.previousStock = previousStock;
        this.changeQuantity = changeQuantity;
        this.newStock = newStock;
        this.reason = reason;
        this.updatedBy = updatedBy;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public int getPreviousStock() { return previousStock; }
    public void setPreviousStock(int previousStock) { this.previousStock = previousStock; }

    public int getChangeQuantity() { return changeQuantity; }
    public void setChangeQuantity(int changeQuantity) { this.changeQuantity = changeQuantity; }

    public int getNewStock() { return newStock; }
    public void setNewStock(int newStock) { this.newStock = newStock; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
