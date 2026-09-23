package com.lunette.gifts.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(length = 100)
    private String productName;

    @Column(length = 100)
    private String variantName;

    @Column(length = 50)
    private String variantDimensions;

    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    private boolean customizationSelected = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal customizationCharge = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String customizationInstructions;

    private int requiredPhotos;
    private int uploadedPhotos;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal itemSubtotal;

    @OneToMany(mappedBy = "orderItem", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<CustomerPhoto> photos = new ArrayList<>();

    public OrderItem() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public String getVariantDimensions() { return variantDimensions; }
    public void setVariantDimensions(String variantDimensions) { this.variantDimensions = variantDimensions; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public boolean isCustomizationSelected() { return customizationSelected; }
    public void setCustomizationSelected(boolean customizationSelected) { this.customizationSelected = customizationSelected; }

    public BigDecimal getCustomizationCharge() { return customizationCharge; }
    public void setCustomizationCharge(BigDecimal customizationCharge) { this.customizationCharge = customizationCharge; }

    public String getCustomizationInstructions() { return customizationInstructions; }
    public void setCustomizationInstructions(String customizationInstructions) { this.customizationInstructions = customizationInstructions; }

    public int getRequiredPhotos() { return requiredPhotos; }
    public void setRequiredPhotos(int requiredPhotos) { this.requiredPhotos = requiredPhotos; }

    public int getUploadedPhotos() { return uploadedPhotos; }
    public void setUploadedPhotos(int uploadedPhotos) { this.uploadedPhotos = uploadedPhotos; }

    public BigDecimal getItemSubtotal() { return itemSubtotal; }
    public void setItemSubtotal(BigDecimal itemSubtotal) { this.itemSubtotal = itemSubtotal; }

    public List<CustomerPhoto> getPhotos() { return photos; }
    public void setPhotos(List<CustomerPhoto> photos) { this.photos = photos; }

    public void addPhoto(CustomerPhoto photo) {
        photos.add(photo);
        photo.setOrderItem(this);
    }
}
