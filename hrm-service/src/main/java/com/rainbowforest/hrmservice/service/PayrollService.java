package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.PayrollDto;

import java.math.BigDecimal;
import java.util.List;

public interface PayrollService {
    PayrollDto.Response calculate(PayrollDto.CalculateRequest request);
    PayrollDto.Response calculateAll(int month, int year);
    PayrollDto.Response getById(Long id);
    List<PayrollDto.Response> getByEmployee(Long employeeId);
    List<PayrollDto.Response> getByMonthAndYear(int month, int year);
    PayrollDto.Response approve(Long id, Long approverId);
    PayrollDto.Response markPaid(Long id);
    BigDecimal getTotalPayrollByMonthAndYear(int month, int year);
}
