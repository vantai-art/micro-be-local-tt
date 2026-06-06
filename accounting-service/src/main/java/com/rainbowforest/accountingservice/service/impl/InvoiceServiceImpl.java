package com.rainbowforest.accountingservice.service.impl;

import com.rainbowforest.accountingservice.domain.Invoice;
import com.rainbowforest.accountingservice.domain.InvoiceItem;
import com.rainbowforest.accountingservice.dto.InvoiceDto;
import com.rainbowforest.accountingservice.enums.InvoiceStatus;
import com.rainbowforest.accountingservice.enums.InvoiceType;
import com.rainbowforest.accountingservice.repository.InvoiceItemRepository;
import com.rainbowforest.accountingservice.repository.InvoiceRepository;
import com.rainbowforest.accountingservice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceItemRepository invoiceItemRepository;

    @Override
    @Transactional
    public InvoiceDto.Response create(InvoiceDto.Request request) {
        String invoiceNumber = generateInvoiceNumber(request.getType());

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .type(request.getType())
                .status(InvoiceStatus.DRAFT)
                .partnerName(request.getPartnerName())
                .partnerTaxCode(request.getPartnerTaxCode())
                .partnerAddress(request.getPartnerAddress())
                .partnerEmail(request.getPartnerEmail())
                .partnerPhone(request.getPartnerPhone())
                .orderId(request.getOrderId())
                .issueDate(request.getIssueDate())
                .dueDate(request.getDueDate())
                .taxRate(request.getTaxRate() != null ? request.getTaxRate() : new BigDecimal("10.00"))
                .discount(request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO)
                .currency(request.getCurrency() != null ? request.getCurrency() : "VND")
                .note(request.getNote())
                .createdBy(request.getCreatedBy())
                .build();

        // Tính subtotal từ items
        if (request.getItems() != null && !request.getItems().isEmpty()) {
            invoice.setSubtotal(request.getItems().stream()
                    .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        } else {
            invoice.setSubtotal(BigDecimal.ZERO);
        }

        invoice = invoiceRepository.save(invoice);

        // Lưu invoice items
        if (request.getItems() != null) {
            final Invoice savedInvoice = invoice;
            List<InvoiceItem> items = request.getItems().stream()
                    .map(i -> InvoiceItem.builder()
                            .invoice(savedInvoice)
                            .description(i.getDescription())
                            .unit(i.getUnit())
                            .quantity(i.getQuantity() != null ? i.getQuantity() : BigDecimal.ONE)
                            .unitPrice(i.getUnitPrice() != null ? i.getUnitPrice() : BigDecimal.ZERO)
                            .build())
                    .collect(Collectors.toList());
            invoiceItemRepository.saveAll(items);
        }

        log.info("Tạo hóa đơn thành công: {}", invoiceNumber);
        return toResponse(invoiceRepository.findById(invoice.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public InvoiceDto.Response update(Long id, InvoiceDto.Request request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn id: " + id));

        if (invoice.getStatus() == InvoiceStatus.PAID || invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException("Không thể cập nhật hóa đơn đã thanh toán hoặc đã hủy");
        }

        invoice.setPartnerName(request.getPartnerName());
        invoice.setPartnerTaxCode(request.getPartnerTaxCode());
        invoice.setPartnerAddress(request.getPartnerAddress());
        invoice.setPartnerEmail(request.getPartnerEmail());
        invoice.setPartnerPhone(request.getPartnerPhone());
        invoice.setIssueDate(request.getIssueDate());
        invoice.setDueDate(request.getDueDate());
        if (request.getTaxRate() != null) invoice.setTaxRate(request.getTaxRate());
        if (request.getDiscount() != null) invoice.setDiscount(request.getDiscount());
        invoice.setNote(request.getNote());

        // Cập nhật items
        if (request.getItems() != null) {
            invoiceItemRepository.deleteByInvoiceId(id);
            final Invoice fi = invoice;
            BigDecimal subtotal = request.getItems().stream()
                    .map(i -> i.getQuantity().multiply(i.getUnitPrice()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            invoice.setSubtotal(subtotal);
            List<InvoiceItem> items = request.getItems().stream()
                    .map(i -> InvoiceItem.builder()
                            .invoice(fi)
                            .description(i.getDescription())
                            .unit(i.getUnit())
                            .quantity(i.getQuantity() != null ? i.getQuantity() : BigDecimal.ONE)
                            .unitPrice(i.getUnitPrice() != null ? i.getUnitPrice() : BigDecimal.ZERO)
                            .build())
                    .collect(Collectors.toList());
            invoiceItemRepository.saveAll(items);
        }

        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    public InvoiceDto.Response getById(Long id) {
        return toResponse(invoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn id: " + id)));
    }

    @Override
    public InvoiceDto.Response getByNumber(String invoiceNumber) {
        return toResponse(invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn số: " + invoiceNumber)));
    }

    @Override
    public List<InvoiceDto.Response> getAll() {
        return invoiceRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto.Response> getByType(InvoiceType type) {
        return invoiceRepository.findByType(type).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto.Response> getByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto.Response> getByDateRange(LocalDate from, LocalDate to) {
        return invoiceRepository.findByIssueDateBetween(from, to).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto.Response> getOverdue() {
        // Đánh dấu các hóa đơn quá hạn
        List<Invoice> overdues = invoiceRepository.findOverdue(LocalDate.now());
        overdues.forEach(inv -> inv.setStatus(InvoiceStatus.OVERDUE));
        invoiceRepository.saveAll(overdues);
        return overdues.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<InvoiceDto.Response> searchByPartner(String keyword) {
        return invoiceRepository.findByPartnerNameContainingIgnoreCase(keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoiceDto.Response changeStatus(Long id, InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn id: " + id));
        invoice.setStatus(status);
        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public InvoiceDto.Response markPaid(Long id, LocalDate paidDate) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn id: " + id));
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidDate(paidDate != null ? paidDate : LocalDate.now());
        return toResponse(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy hóa đơn id: " + id));
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("Không thể xóa hóa đơn đã thanh toán");
        }
        invoiceItemRepository.deleteByInvoiceId(id);
        invoiceRepository.delete(invoice);
        log.info("Đã xóa hóa đơn id: {}", id);
    }

    @Override
    public BigDecimal getTotalRevenue(LocalDate from, LocalDate to) {
        return invoiceRepository.sumPaidByTypeAndDateRange(InvoiceType.INCOME, from, to);
    }

    @Override
    public BigDecimal getTotalExpenseInvoice(LocalDate from, LocalDate to) {
        return invoiceRepository.sumPaidByTypeAndDateRange(InvoiceType.EXPENSE, from, to);
    }

    // ── HELPERS ──────────────────────────────────────────────────────────────

    private String generateInvoiceNumber(InvoiceType type) {
        String prefix = type == InvoiceType.INCOME ? "INV-IN" : "INV-EX";
        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        long count = invoiceRepository.count() + 1;
        String candidate = String.format("%s-%s-%04d", prefix, datePart, count);
        while (invoiceRepository.existsByInvoiceNumber(candidate)) {
            count++;
            candidate = String.format("%s-%s-%04d", prefix, datePart, count);
        }
        return candidate;
    }

    private InvoiceDto.Response toResponse(Invoice inv) {
        List<InvoiceItem> items = invoiceItemRepository.findByInvoiceId(inv.getId());
        List<InvoiceDto.ItemResponse> itemResponses = items.stream()
                .map(i -> InvoiceDto.ItemResponse.builder()
                        .id(i.getId())
                        .description(i.getDescription())
                        .unit(i.getUnit())
                        .quantity(i.getQuantity())
                        .unitPrice(i.getUnitPrice())
                        .amount(i.getAmount())
                        .build())
                .collect(Collectors.toList());

        return InvoiceDto.Response.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .type(inv.getType())
                .status(inv.getStatus())
                .partnerName(inv.getPartnerName())
                .partnerTaxCode(inv.getPartnerTaxCode())
                .partnerAddress(inv.getPartnerAddress())
                .partnerEmail(inv.getPartnerEmail())
                .partnerPhone(inv.getPartnerPhone())
                .orderId(inv.getOrderId())
                .issueDate(inv.getIssueDate())
                .dueDate(inv.getDueDate())
                .paidDate(inv.getPaidDate())
                .subtotal(inv.getSubtotal())
                .taxRate(inv.getTaxRate())
                .taxAmount(inv.getTaxAmount())
                .discount(inv.getDiscount())
                .totalAmount(inv.getTotalAmount())
                .currency(inv.getCurrency())
                .note(inv.getNote())
                .createdBy(inv.getCreatedBy())
                .approvedBy(inv.getApprovedBy())
                .createdAt(inv.getCreatedAt())
                .updatedAt(inv.getUpdatedAt())
                .items(itemResponses)
                .build();
    }
}
