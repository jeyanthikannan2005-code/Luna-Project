package com.lunette.gifts.service;

import com.lunette.gifts.entity.*;
import com.lunette.gifts.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ExpenseRepository expenseRepository;
    private final WorkerActivityRepository activityRepository;
    private final UserAccountRepository userRepository;

    public ReportService(OrderRepository orderRepository,
                         ProductRepository productRepository,
                         ExpenseRepository expenseRepository,
                         WorkerActivityRepository activityRepository,
                         UserAccountRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.expenseRepository = expenseRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public byte[] generateOrdersCsv() {
        List<Order> orders = orderRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("Order Number,Date,Customer Name,Phone,Email,PIN Code,Zone,Subtotal,Delivery,Total,Payment Status,Order Status\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (Order o : orders) {
            csv.append("\"").append(o.getOrderNumber()).append("\",")
               .append("\"").append(o.getCreatedAt().format(dtf)).append("\",")
               .append("\"").append(o.getCustomerName()).append("\",")
               .append("\"").append(o.getCustomerPhone()).append("\",")
               .append("\"").append(o.getCustomerEmail() != null ? o.getCustomerEmail() : "").append("\",")
               .append("\"").append(o.getPinCode()).append("\",")
               .append("\"").append(o.getDeliveryZone()).append("\",")
               .append(o.getSubtotal()).append(",")
               .append(o.getDeliveryCharge()).append(",")
               .append(o.getTotalAmount()).append(",")
               .append("\"").append(o.getPaymentStatus()).append("\",")
               .append("\"").append(o.getOrderStatus()).append("\"\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateInventoryCsv() {
        List<Product> products = productRepository.findAll();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Product Name,Category,Base Price,Stock Quantity,Low Stock Threshold,Status\n");

        for (Product p : products) {
            String status = p.getStockQuantity() <= p.getLowStockThreshold() ? "LOW STOCK" : "IN STOCK";
            csv.append(p.getId()).append(",")
               .append("\"").append(p.getName()).append("\",")
               .append("\"").append(p.getCategory()).append("\",")
               .append(p.getBasePrice()).append(",")
               .append(p.getStockQuantity()).append(",")
               .append(p.getLowStockThreshold()).append(",")
               .append("\"").append(status).append("\"\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateExpensesCsv() {
        List<Expense> expenses = expenseRepository.findAllByOrderByExpenseDateDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Date,Category,Description,Amount,Recorded By\n");

        for (Expense e : expenses) {
            csv.append(e.getId()).append(",")
               .append(e.getExpenseDate()).append(",")
               .append("\"").append(e.getCategory()).append("\",")
               .append("\"").append(e.getDescription()).append("\",")
               .append(e.getAmount()).append(",")
               .append("\"").append(e.getRecordedBy() != null ? e.getRecordedBy() : "").append("\"\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] generateWorkerActivityCsv() {
        List<WorkerActivity> activities = activityRepository.findAllByOrderByTimestampDesc();
        StringBuilder csv = new StringBuilder();
        csv.append("ID,Timestamp,Worker Username,Worker Name,Activity Type,Order Number,Details\n");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        for (WorkerActivity a : activities) {
            csv.append(a.getId()).append(",")
               .append(a.getTimestamp().format(dtf)).append(",")
               .append("\"").append(a.getWorkerUsername()).append("\",")
               .append("\"").append(a.getWorkerFullName()).append("\",")
               .append("\"").append(a.getActivityType()).append("\",")
               .append("\"").append(a.getOrderNumber() != null ? a.getOrderNumber() : "").append("\",")
               .append("\"").append(a.getDetails() != null ? a.getDetails().replace("\"", "\"\"") : "").append("\"\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }
}
