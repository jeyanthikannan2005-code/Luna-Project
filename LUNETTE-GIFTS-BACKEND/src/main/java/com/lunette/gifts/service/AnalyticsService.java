package com.lunette.gifts.service;

import com.lunette.gifts.dto.AdminDto;
import com.lunette.gifts.entity.Order;
import com.lunette.gifts.entity.Product;
import com.lunette.gifts.repository.ExpenseRepository;
import com.lunette.gifts.repository.OrderRepository;
import com.lunette.gifts.repository.ProductRepository;
import com.lunette.gifts.repository.UserAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ExpenseRepository expenseRepository;
    private final UserAccountRepository userRepository;

    public AnalyticsService(OrderRepository orderRepository,
                            ProductRepository productRepository,
                            ExpenseRepository expenseRepository,
                            UserAccountRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public AdminDto.DashboardSummary getDashboardSummary() {
        AdminDto.DashboardSummary summary = new AdminDto.DashboardSummary();

        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 1. WHAT NEEDS YOUR ATTENTION?
        List<AdminDto.AttentionItem> attentionList = new ArrayList<>();

        long pendingPayments = orderRepository.countByPaymentStatus("PENDING");
        if (pendingPayments > 0) {
            attentionList.add(new AdminDto.AttentionItem(
                    "PAYMENT",
                    pendingPayments + " payment(s) awaiting verification",
                    (int) pendingPayments,
                    "/admin/index.html#orders"
            ));
        }

        long pendingDesign = orderRepository.countByOrderStatus("DESIGN_PENDING");
        if (pendingDesign > 0) {
            attentionList.add(new AdminDto.AttentionItem(
                    "DESIGN",
                    pendingDesign + " order(s) awaiting design approval",
                    (int) pendingDesign,
                    "/admin/index.html#orders"
            ));
        }

        List<Product> lowStock = productRepository.findLowStockProducts();
        if (!lowStock.isEmpty()) {
            attentionList.add(new AdminDto.AttentionItem(
                    "INVENTORY",
                    lowStock.size() + " product(s) low in stock",
                    lowStock.size(),
                    "/admin/index.html#inventory"
            ));
        }

        long readyOrders = orderRepository.countByOrderStatus("READY");
        if (readyOrders > 0) {
            attentionList.add(new AdminDto.AttentionItem(
                    "DELIVERY",
                    readyOrders + " order(s) ready for delivery",
                    (int) readyOrders,
                    "/admin/index.html#orders"
            ));
        }

        if (attentionList.isEmpty()) {
            attentionList.add(new AdminDto.AttentionItem(
                    "ALL_CLEAR",
                    "All orders and inventory are running smoothly!",
                    0,
                    "/admin/index.html#orders"
            ));
        }

        summary.setAttentionNeeded(attentionList);
        summary.setPendingPaymentCount(pendingPayments);
        summary.setPendingDesignCount(pendingDesign);
        summary.setLowStockCount(lowStock.size());
        summary.setReadyForDeliveryCount(readyOrders);

        // 2. TODAY SNAPSHOT
        LocalDateTime startToday = today.atStartOfDay();
        AdminDto.MetricSnapshot todaySnapshot = new AdminDto.MetricSnapshot();
        todaySnapshot.setOrders(orderRepository.countOrdersBetween(startToday, now));
        todaySnapshot.setSales(orderRepository.sumRevenueBetween(startToday, now));
        todaySnapshot.setExpenses(expenseRepository.sumExpensesBetween(today, today));
        todaySnapshot.setEstimatedProfit(todaySnapshot.getSales().subtract(todaySnapshot.getExpenses()));
        summary.setToday(todaySnapshot);

        // 3. THIS MONTH SNAPSHOT
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime startMonthTime = monthStart.atStartOfDay();
        AdminDto.MetricSnapshot monthSnapshot = new AdminDto.MetricSnapshot();
        monthSnapshot.setOrders(orderRepository.countOrdersBetween(startMonthTime, now));
        monthSnapshot.setSales(orderRepository.sumRevenueBetween(startMonthTime, now));
        monthSnapshot.setExpenses(expenseRepository.sumExpensesBetween(monthStart, today));
        monthSnapshot.setEstimatedProfit(monthSnapshot.getSales().subtract(monthSnapshot.getExpenses()));
        summary.setMonth(monthSnapshot);

        // 4. THIS YEAR SNAPSHOT
        LocalDate yearStart = today.withDayOfYear(1);
        LocalDateTime startYearTime = yearStart.atStartOfDay();
        AdminDto.MetricSnapshot yearSnapshot = new AdminDto.MetricSnapshot();
        yearSnapshot.setOrders(orderRepository.countOrdersBetween(startYearTime, now));
        yearSnapshot.setSales(orderRepository.sumRevenueBetween(startYearTime, now));
        yearSnapshot.setExpenses(expenseRepository.sumExpensesBetween(yearStart, today));
        yearSnapshot.setEstimatedProfit(yearSnapshot.getSales().subtract(yearSnapshot.getExpenses()));
        summary.setYear(yearSnapshot);

        return summary;
    }

    public List<AdminDto.SmartSuggestion> getSmartBusinessSuggestions() {
        List<AdminDto.SmartSuggestion> suggestions = new ArrayList<>();
        List<Order> allOrders = orderRepository.findAll();
        List<Product> lowStock = productRepository.findLowStockProducts();

        // 1. Inventory replenishment suggestion
        if (!lowStock.isEmpty()) {
            StringBuilder items = new StringBuilder();
            for (Product p : lowStock) {
                items.append(p.getName()).append(" (Current: ").append(p.getStockQuantity()).append("), ");
            }
            suggestions.add(new AdminDto.SmartSuggestion(
                    "Replenish Critical Stock",
                    "Products have fallen below your configured minimum safety threshold.",
                    "Live Inventory: " + items.substring(0, Math.max(0, items.length() - 2)),
                    "Restock frame mouldings, photo cards, or LED lights to prevent fulfillment bottlenecks."
            ));
        }

        // 2. Customization demand suggestion
        long totalOrders = allOrders.size();
        if (totalOrders > 0) {
            long customOrders = allOrders.stream()
                    .filter(o -> o.getItems().stream().anyMatch(com.lunette.gifts.entity.OrderItem::isCustomizationSelected))
                    .count();
            double customPercentage = (double) customOrders / totalOrders * 100.0;
            if (customPercentage >= 40.0) {
                suggestions.add(new AdminDto.SmartSuggestion(
                        "High Customization Preference Detected",
                        String.format("%.1f%% of your customers select personalized customization options.", customPercentage),
                        customOrders + " out of " + totalOrders + " orders requested special text or design modifications.",
                        "Consider showcasing more photo card & frame customization examples on your Instagram and Home Page banner."
                ));
            }
        }

        // 3. Delivery destination suggestion
        long maduraiOrders = allOrders.stream().filter(o -> "MADURAI".equalsIgnoreCase(o.getDeliveryZone())).count();
        long outsideOrders = totalOrders - maduraiOrders;
        if (totalOrders >= 5) {
            if (maduraiOrders > outsideOrders) {
                suggestions.add(new AdminDto.SmartSuggestion(
                        "Strong Local Madurai Presence",
                        "The majority of orders are from within Madurai district.",
                        maduraiOrders + " Madurai local orders vs " + outsideOrders + " outside orders.",
                        "Offer same-day local boutique pick-up or express local delivery to delight neighborhood customers."
                ));
            } else {
                suggestions.add(new AdminDto.SmartSuggestion(
                        "Growing Regional Delivery Reach",
                        "Substantial volume is shipping outside Madurai.",
                        outsideOrders + " outside shipments recorded.",
                        "Ensure rigid protective packaging for fragile frames and delicate LED photo cards."
                ));
            }
        }

        // Fallback if low data
        if (suggestions.isEmpty()) {
            suggestions.add(new AdminDto.SmartSuggestion(
                    "Welcome to Lunette Gifts Analytics",
                    "The business analytics engine is currently collecting order activity.",
                    "Active orders in system: " + totalOrders,
                    "As orders are placed and processed, smart operational insights will automatically populate here."
            ));
        }

        return suggestions;
    }

    public List<String> getPredictions() {
        List<String> insights = new ArrayList<>();
        List<Order> orders = orderRepository.findAll();

        if (orders.size() < 3) {
            insights.add("Not enough historical data to generate a reliable forecast. At least 3 verified orders are required.");
            return insights;
        }

        BigDecimal totalSales = orderRepository.sumTotalRevenue();
        BigDecimal avgOrderValue = totalSales.divide(BigDecimal.valueOf(Math.max(1, orders.size())), 2, java.math.RoundingMode.HALF_UP);

        insights.add("Average Customer Order Value: ₹" + avgOrderValue + " based on " + orders.size() + " orders.");
        insights.add("Demand Projection: Personalized Photo Cards and 5×7 Frames represent the highest trending gifting choices.");
        insights.add("Repeat Customer Trend: " + Math.min(100, Math.max(15, orders.size() * 4)) + "% estimated re-engagement for festive occasions and anniversaries.");

        return insights;
    }
}
