package com.lunette.gifts.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String customerName;

    @Column(nullable = false)
    private int rating; // 1 to 5

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(length = 500)
    private String imageUrl;

    private boolean approved = true; // Admin can approve/hide

    private LocalDateTime createdAt = LocalDateTime.now();

    public Review() {}

    public Review(Product product, String customerName, int rating, String comment, String imageUrl, boolean approved) {
        this.product = product;
        this.customerName = customerName;
        this.rating = rating;
        this.comment = comment;
        this.imageUrl = imageUrl;
        this.approved = approved;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
