package com.rainbowforest.accountingservice.repository;

import com.rainbowforest.accountingservice.domain.Expense;
import com.rainbowforest.accountingservice.enums.ExpenseCategory;
import com.rainbowforest.accountingservice.enums.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    boolean existsByExpenseCode(String expenseCode);

    List<Expense> findByStatus(ExpenseStatus status);

    List<Expense> findByCategory(ExpenseCategory category);

    List<Expense> findByDepartmentId(Long departmentId);

    List<Expense> findByRequestedBy(String requestedBy);

    List<Expense> findByExpenseDateBetween(LocalDate from, LocalDate to);

    List<Expense> findByStatusAndCategory(ExpenseStatus status, ExpenseCategory category);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.status = 'PAID' AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumPaidByDateRange(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.status = 'PAID' AND e.category = :category AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumPaidByCategoryAndDateRange(@Param("category") ExpenseCategory category,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.status = 'PAID' AND e.departmentId = :deptId AND e.expenseDate BETWEEN :from AND :to")
    BigDecimal sumPaidByDepartmentAndDateRange(@Param("deptId") Long departmentId,
                                               @Param("from") LocalDate from,
                                               @Param("to") LocalDate to);
}
