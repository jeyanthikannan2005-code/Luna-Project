package com.lunette.gifts.repository;

import com.lunette.gifts.entity.Order;
import com.lunette.gifts.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerOrderByCreatedAtDesc(UserAccount customer);
    List<Order> findByCustomerPhoneOrderByCreatedAtDesc(String customerPhone);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByOrderStatusOrderByCreatedAtDesc(String orderStatus);
    List<Order> findByPaymentStatusOrderByCreatedAtDesc(String paymentStatus);
    List<Order> findByCreatedAtBetweenOrderByCreatedAtDesc(LocalDateTime start, LocalDateTime end);

    long countByOrderStatus(String orderStatus);
    long countByPaymentStatus(String paymentStatus);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end")
    long countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createdAt >= :start AND o.createdAt < :end AND o.paymentStatus = 'CONFIRMED'")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.paymentStatus = 'CONFIRMED'")
    BigDecimal sumTotalRevenue();

    @Query("SELECT o FROM Order o WHERE " +
           "LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "o.customerPhone LIKE CONCAT('%', :query, '%') " +
           "ORDER BY o.createdAt DESC")
    List<Order> searchOrders(@Param("query") String query);
}
