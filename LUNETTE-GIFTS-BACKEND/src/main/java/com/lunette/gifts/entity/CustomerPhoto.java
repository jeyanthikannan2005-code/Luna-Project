package com.lunette.gifts.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_photos")
public class CustomerPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_item_id", nullable = false)
    @JsonBackReference
    private OrderItem orderItem;

    @Column(nullable = false, length = 500)
    private String fileUrl; // Path or storage identifier

    @Column(nullable = false, length = 255)
    private String originalFilename;

    @Column(length = 50)
    private String contentType;

    private long fileSize;

    // PRINT_PHOTO or CUSTOMIZATION_REFERENCE
    @Column(nullable = false, length = 40)
    private String fileType = "PRINT_PHOTO";

    private int slotIndex = 1;

    private LocalDateTime uploadedAt = LocalDateTime.now();

    public CustomerPhoto() {}

    public CustomerPhoto(OrderItem orderItem, String fileUrl, String originalFilename, String contentType, long fileSize, String fileType, int slotIndex) {
        this.orderItem = orderItem;
        this.fileUrl = fileUrl;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.fileType = fileType;
        this.slotIndex = slotIndex;
        this.uploadedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public OrderItem getOrderItem() { return orderItem; }
    public void setOrderItem(OrderItem orderItem) { this.orderItem = orderItem; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public int getSlotIndex() { return slotIndex; }
    public void setSlotIndex(int slotIndex) { this.slotIndex = slotIndex; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
