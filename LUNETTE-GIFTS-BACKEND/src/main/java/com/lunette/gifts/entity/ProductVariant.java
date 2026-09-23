package com.lunette.gifts.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @JsonBackReference
    private Product product;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String dimensions;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // The required number of customer photos for this specific variant (stored in DB, admin-configurable)
    @Column(nullable = false)
    private int requiredPhotos = 1;

    private boolean active = true;

    public ProductVariant() {}

    public ProductVariant(Product product, String name, String dimensions, BigDecimal price, int requiredPhotos) {
        this.product = product;
        this.name = name;
        this.dimensions = dimensions;
        this.price = price;
        this.requiredPhotos = requiredPhotos;
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getRequiredPhotos() { return requiredPhotos; }
    public void setRequiredPhotos(int requiredPhotos) { this.requiredPhotos = requiredPhotos; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
