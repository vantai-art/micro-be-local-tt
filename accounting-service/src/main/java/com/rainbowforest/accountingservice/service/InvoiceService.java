package com.rainbowforest.accountingservice.service;

import com.rainbowforest.accountingservice.dto.InvoiceDto;
import com.rainbowforest.accountingservice.enums.InvoiceStatus;
import com.rainbowforest.accountingservice.enums.InvoiceType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface InvoiceService {
    InvoiceDto.Response create(InvoiceDto.Request request);
    InvoiceDto.Response update(Long id, InvoiceDto.Request request);
    InvoiceDto.Response getById(Long id);
    InvoiceDto.Response getByNumber(String invoiceNumber);
    List<InvoiceDto.Response> getAll();
    List<InvoiceDto.Response> getByType(InvoiceType type);
    List<InvoiceDto.Response> getByStatus(InvoiceStatus status);
    List<InvoiceDto.Response> getByDateRange(LocalDate from, LocalDate to);
    List<InvoiceDto.Response> getOverdue();
    List<InvoiceDto.Response> searchByPartner(String keyword);
    InvoiceDto.Response changeStatus(Long id, InvoiceStatus status);
    InvoiceDto.Response markPaid(Long id, LocalDate paidDate);
    void delete(Long id);
    BigDecimal getTotalRevenue(LocalDate from, LocalDate to);
    BigDecimal getTotalExpenseInvoice(LocalDate from, LocalDate to);
}
