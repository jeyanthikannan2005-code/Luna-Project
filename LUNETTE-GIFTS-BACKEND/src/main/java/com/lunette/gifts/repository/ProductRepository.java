package com.lunette.gifts.repository;

import com.lunette.gifts.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findBySlug(String slug);
    List<Product> findByActiveTrueOrderByIdAsc();
    List<Product> findByCategoryAndActiveTrue(String category);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.lowStockThreshold AND p.active = true")
    List<Product> findLowStockProducts();
}
