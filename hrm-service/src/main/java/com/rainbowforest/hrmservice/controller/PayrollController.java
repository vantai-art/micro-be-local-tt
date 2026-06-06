package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.PayrollDto;
import com.rainbowforest.hrmservice.service.PayrollService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/hrm/payrolls")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    // Tính lương cho 1 nhân viên
    @PostMapping("/calculate")
    public ResponseEntity<PayrollDto.Response> calculate(@RequestBody PayrollDto.CalculateRequest request) {
        return ResponseEntity.ok(payrollService.calculate(request));
    }

    // Tính lương hàng loạt tất cả nhân viên active trong tháng
    @PostMapping("/calculate-all")
    public ResponseEntity<PayrollDto.Response> calculateAll(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.calculateAll(month, year));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PayrollDto.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<PayrollDto.Response>> getByEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(payrollService.getByEmployee(employeeId));
    }

    @GetMapping
    public ResponseEntity<List<PayrollDto.Response>> getByMonthAndYear(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getByMonthAndYear(month, year));
    }

    // Duyệt bảng lương
    @PatchMapping("/{id}/approve")
    public ResponseEntity<PayrollDto.Response> approve(
            @PathVariable Long id,
            @RequestParam Long approverId) {
        return ResponseEntity.ok(payrollService.approve(id, approverId));
    }

    // Đánh dấu đã thanh toán
    @PatchMapping("/{id}/paid")
    public ResponseEntity<PayrollDto.Response> markPaid(@PathVariable Long id) {
        return ResponseEntity.ok(payrollService.markPaid(id));
    }

    // Tổng quỹ lương tháng
    @GetMapping("/total")
    public ResponseEntity<BigDecimal> getTotalPayroll(
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(payrollService.getTotalPayrollByMonthAndYear(month, year));
    }
}
