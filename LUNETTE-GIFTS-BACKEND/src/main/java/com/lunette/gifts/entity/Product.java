package com.lunette.gifts.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String slug;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 50)
    private String category; // PHOTO_FRAMES, PHOTO_CARDS, RING_ALBUMS, LED_POLAROIDS

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    private BigDecimal customizationPrice = BigDecimal.ZERO;

    private int minQuantity = 1;

    // Photo rule: FIXED_PER_VARIANT (Frames), ONE_PER_UNIT (Cards, Albums, Polaroids)
    @Column(length = 30)
    private String photoRule = "FIXED_PER_VARIANT";

    private boolean hasLightOption = false;
    @Column(precision = 10, scale = 2)
    private BigDecimal lightPrice = BigDecimal.ZERO;

    private boolean hasRingOption = false;
    @Column(precision = 10, scale = 2)
    private BigDecimal ringPrice = BigDecimal.ZERO;

    @Column(length = 500)
    private String imageUrl;

    private int stockQuantity = 100;
    private int lowStockThreshold = 15;
    private boolean active = true;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<ProductVariant> variants = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();

    public Product() {}

    public Product(String slug, String name, String category, String description, BigDecimal basePrice,
                   BigDecimal customizationPrice, int minQuantity, String photoRule, String imageUrl) {
        this.slug = slug;
        this.name = name;
        this.category = category;
        this.description = description;
        this.basePrice = basePrice;
        this.customizationPrice = customizationPrice;
        this.minQuantity = minQuantity;
        this.photoRule = photoRule;
        this.imageUrl = imageUrl;
        this.createdAt = LocalDateTime.now();
        this.active = true;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

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

    public boolean isHasLightOption() { return hasLightOption; }
    public void setHasLightOption(boolean hasLightOption) { this.hasLightOption = hasLightOption; }

    public BigDecimal getLightPrice() { return lightPrice; }
    public void setLightPrice(BigDecimal lightPrice) { this.lightPrice = lightPrice; }

    public boolean isHasRingOption() { return hasRingOption; }
    public void setHasRingOption(boolean hasRingOption) { this.hasRingOption = hasRingOption; }

    public BigDecimal getRingPrice() { return ringPrice; }
    public void setRingPrice(BigDecimal ringPrice) { this.ringPrice = ringPrice; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getLowStockThreshold() { return lowStockThreshold; }
    public void setLowStockThreshold(int lowStockThreshold) { this.lowStockThreshold = lowStockThreshold; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public void addVariant(ProductVariant variant) {
        variants.add(variant);
        variant.setProduct(this);
    }
}
