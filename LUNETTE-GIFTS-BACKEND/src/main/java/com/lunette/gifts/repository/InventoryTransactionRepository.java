package com.lunette.gifts.repository;

import com.lunette.gifts.entity.InventoryTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {
    List<InventoryTransaction> findAllByOrderByTimestampDesc();
    List<InventoryTransaction> findByProductIdOrderByTimestampDesc(Long productId);
}
