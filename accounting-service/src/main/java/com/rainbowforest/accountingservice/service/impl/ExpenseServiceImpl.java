package com.rainbowforest.accountingservice.service.impl;

import com.rainbowforest.accountingservice.domain.Expense;
import com.rainbowforest.accountingservice.dto.ExpenseDto;
import com.rainbowforest.accountingservice.enums.ExpenseCategory;
import com.rainbowforest.accountingservice.enums.ExpenseStatus;
import com.rainbowforest.accountingservice.repository.ExpenseRepository;
import com.rainbowforest.accountingservice.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Override
    @Transactional
    public ExpenseDto.Response create(ExpenseDto.Request request) {
        String code = generateExpenseCode();

        Expense expense = Expense.builder()
                .expenseCode(code)
                .title(request.getTitle())
                .description(request.getDescription())
                .category(request.getCategory())
                .status(ExpenseStatus.PENDING)
                .amount(request.getAmount())
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .expenseDate(request.getExpenseDate())
                .vendor(request.getVendor())
                .receiptUrl(request.getReceiptUrl())
                .departmentId(request.getDepartmentId())
                .departmentName(request.getDepartmentName())
                .requestedBy(request.getRequestedBy())
                .note(request.getNote())
                .build();

        expense = expenseRepository.save(expense);
        log.info("Tạo chi phí thành công: {}", code);
        return toResponse(expense);
    }

    @Override
    @Transactional
    public ExpenseDto.Response update(Long id, ExpenseDto.Request request) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi phí id: " + id));

        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new IllegalStateException("Không thể cập nhật chi phí đã thanh toán");
        }

        expense.setTitle(request.getTitle());
        expense.setDescription(request.getDescription());
        expense.setCategory(request.getCategory());
        expense.setAmount(request.getAmount());
        if (request.getCurrency() != null) expense.setCurrency(request.getCurrency());
        expense.setExpenseDate(request.getExpenseDate());
        expense.setVendor(request.getVendor());
        expense.setReceiptUrl(request.getReceiptUrl());
        expense.setDepartmentId(request.getDepartmentId());
        expense.setDepartmentName(request.getDepartmentName());
        expense.setNote(request.getNote());

        return toResponse(expenseRepository.save(expense));
    }

    @Override
    public ExpenseDto.Response getById(Long id) {
        return toResponse(expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi phí id: " + id)));
    }

    @Override
    public List<ExpenseDto.Response> getAll() {
        return expenseRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDto.Response> getByStatus(ExpenseStatus status) {
        return expenseRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDto.Response> getByCategory(ExpenseCategory category) {
        return expenseRepository.findByCategory(category).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDto.Response> getByDepartment(Long departmentId) {
        return expenseRepository.findByDepartmentId(departmentId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ExpenseDto.Response> getByDateRange(LocalDate from, LocalDate to) {
        return expenseRepository.findByExpenseDateBetween(from, to).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ExpenseDto.Response approve(Long id, String approvedBy) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi phí id: " + id));
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể duyệt chi phí ở trạng thái PENDING");
        }
        expense.setStatus(ExpenseStatus.APPROVED);
        expense.setApprovedBy(approvedBy);
        log.info("Duyệt chi phí {} bởi {}", expense.getExpenseCode(), approvedBy);
        return toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public ExpenseDto.Response reject(Long id, String rejectedBy, String reason) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi phí id: " + id));
        if (expense.getStatus() != ExpenseStatus.PENDING) {
            throw new IllegalStateException("Chỉ có thể từ chối chi phí ở trạng thái PENDING");
        }
        expense.setStatus(ExpenseStatus.REJECTED);
        expense.setApprovedBy(rejectedBy);
        if (reason != null) expense.setNote((expense.getNote() != null ? expense.getNote() + " | " : "") + "Từ chối: " + reason);
        log.info("Từ chối chi phí {} bởi {}", expense.getExpenseCode(), rejectedBy);
        return toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public ExpenseDto.Response markPaid(Long id, LocalDate paymentDate) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi phí id: " + id));
        if (expense.getStatus() != ExpenseStatus.APPROVED) {
            throw new IllegalStateException("Chỉ có thể thanh toán chi phí đã được duyệt");
        }
        expense.setStatus(ExpenseStatus.PAID);
        expense.setPaymentDate(paymentDate != null ? paymentDate : LocalDate.now());
        log.info("Thanh toán chi phí: {}", expense.getExpenseCode());
        return toResponse(expenseRepository.save(expense));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi phí id: " + id));
        if (expense.getStatus() == ExpenseStatus.PAID) {
            throw new IllegalStateException("Không thể xóa chi phí đã thanh toán");
        }
        expenseRepository.delete(expense);
        log.info("Đã xóa chi phí id: {}", id);
    }

    @Override
    public BigDecimal getTotalPaid(LocalDate from, LocalDate to) {
        return expenseRepository.sumPaidByDateRange(from, to);
    }

    @Override
    public BigDecimal getTotalByCategory(ExpenseCategory category, LocalDate from, LocalDate to) {
        return expenseRepository.sumPaidByCategoryAndDateRange(category, from, to);
    }

    @Override
    public BigDecimal getTotalByDepartment(Long departmentId, LocalDate from, LocalDate to) {
        return expenseRepository.sumPaidByDepartmentAndDateRange(departmentId, from, to);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private String generateExpenseCode() {
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = expenseRepository.count() + 1;
        String candidate = String.format("EXP-%s-%04d", datePart, count);
        while (expenseRepository.existsByExpenseCode(candidate)) {
            count++;
            candidate = String.format("EXP-%s-%04d", datePart, count);
        }
        return candidate;
    }

    private ExpenseDto.Response toResponse(Expense e) {
        return ExpenseDto.Response.builder()
                .id(e.getId())
                .expenseCode(e.getExpenseCode())
                .title(e.getTitle())
                .description(e.getDescription())
                .category(e.getCategory())
                .status(e.getStatus())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .expenseDate(e.getExpenseDate())
                .paymentDate(e.getPaymentDate())
                .vendor(e.getVendor())
                .receiptUrl(e.getReceiptUrl())
                .departmentId(e.getDepartmentId())
                .departmentName(e.getDepartmentName())
                .requestedBy(e.getRequestedBy())
                .approvedBy(e.getApprovedBy())
                .note(e.getNote())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}
