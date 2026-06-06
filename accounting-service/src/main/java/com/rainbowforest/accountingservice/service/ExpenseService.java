package com.rainbowforest.accountingservice.service;

import com.rainbowforest.accountingservice.dto.ExpenseDto;
import com.rainbowforest.accountingservice.enums.ExpenseCategory;
import com.rainbowforest.accountingservice.enums.ExpenseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    ExpenseDto.Response create(ExpenseDto.Request request);
    ExpenseDto.Response update(Long id, ExpenseDto.Request request);
    ExpenseDto.Response getById(Long id);
    List<ExpenseDto.Response> getAll();
    List<ExpenseDto.Response> getByStatus(ExpenseStatus status);
    List<ExpenseDto.Response> getByCategory(ExpenseCategory category);
    List<ExpenseDto.Response> getByDepartment(Long departmentId);
    List<ExpenseDto.Response> getByDateRange(LocalDate from, LocalDate to);
    ExpenseDto.Response approve(Long id, String approvedBy);
    ExpenseDto.Response reject(Long id, String rejectedBy, String reason);
    ExpenseDto.Response markPaid(Long id, LocalDate paymentDate);
    void delete(Long id);
    BigDecimal getTotalPaid(LocalDate from, LocalDate to);
    BigDecimal getTotalByCategory(ExpenseCategory category, LocalDate from, LocalDate to);
    BigDecimal getTotalByDepartment(Long departmentId, LocalDate from, LocalDate to);
}
