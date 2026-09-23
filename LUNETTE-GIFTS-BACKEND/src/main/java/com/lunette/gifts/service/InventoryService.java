package com.lunette.gifts.service;

import com.lunette.gifts.dto.AdminDto;
import com.lunette.gifts.entity.InventoryTransaction;
import com.lunette.gifts.entity.Product;
import com.lunette.gifts.repository.InventoryTransactionRepository;
import com.lunette.gifts.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final ProductRepository productRepository;
    private final InventoryTransactionRepository transactionRepository;

    public InventoryService(ProductRepository productRepository,
                            InventoryTransactionRepository transactionRepository) {
        this.productRepository = productRepository;
        this.transactionRepository = transactionRepository;
    }

    public List<Product> getAllInventory() {
        return productRepository.findAll();
    }

    public List<Product> getLowStockAlerts() {
        return productRepository.findLowStockProducts();
    }

    public List<InventoryTransaction> getTransactions() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }

    public Product adjustStock(Long productId, AdminDto.InventoryAdjustmentRequest req, String username) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        int oldStock = product.getStockQuantity();
        int change = req.getChangeQuantity();
        int newStock = Math.max(0, oldStock + change);

        product.setStockQuantity(newStock);
        product = productRepository.save(product);

        InventoryTransaction tx = new InventoryTransaction(
                product,
                oldStock,
                change,
                newStock,
                req.getReason() != null ? req.getReason() : "Manual stock adjustment",
                username
        );
        transactionRepository.save(tx);

        return product;
    }
}
