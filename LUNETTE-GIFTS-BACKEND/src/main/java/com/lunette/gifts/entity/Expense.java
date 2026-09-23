package com.lunette.gifts.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MATERIAL, PACKAGING, DELIVERY, ADVERTISING, ELECTRICITY, OTHER
    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate = LocalDate.now();

    @Column(nullable = false, length = 200)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(length = 60)
    private String recordedBy; // username

    private LocalDateTime createdAt = LocalDateTime.now();

    public Expense() {}

    public Expense(String category, BigDecimal amount, LocalDate expenseDate, String description, String notes, String recordedBy) {
        this.category = category;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.description = description;
        this.notes = notes;
        this.recordedBy = recordedBy;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public String getRecordedBy() { return recordedBy; }
    public void setRecordedBy(String recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
