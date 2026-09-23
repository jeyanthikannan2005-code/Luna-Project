package com.lunette.gifts.service;

import com.lunette.gifts.dto.AdminDto;
import com.lunette.gifts.entity.Expense;
import com.lunette.gifts.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> getAllExpenses() {
        return expenseRepository.findAllByOrderByExpenseDateDesc();
    }

    public List<Expense> getExpensesBetween(LocalDate start, LocalDate end) {
        return expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(start, end);
    }

    public Expense addExpense(AdminDto.ExpenseRequest req, String username) {
        Expense expense = new Expense(
                req.getCategory() != null ? req.getCategory().toUpperCase() : "OTHER",
                req.getAmount(),
                req.getExpenseDate() != null ? req.getExpenseDate() : LocalDate.now(),
                req.getDescription(),
                req.getNotes(),
                username
        );
        return expenseRepository.save(expense);
    }

    public void deleteExpense(Long id) {
        expenseRepository.deleteById(id);
    }

    public BigDecimal sumExpensesBetween(LocalDate start, LocalDate end) {
        return expenseRepository.sumExpensesBetween(start, end);
    }

    public BigDecimal sumTotalExpenses() {
        return expenseRepository.sumTotalExpenses();
    }
}
