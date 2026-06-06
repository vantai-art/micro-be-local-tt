package com.rainbowforest.accountingservice.repository;

import com.rainbowforest.accountingservice.domain.Invoice;
import com.rainbowforest.accountingservice.enums.InvoiceStatus;
import com.rainbowforest.accountingservice.enums.InvoiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByType(InvoiceType type);

    List<Invoice> findByStatus(InvoiceStatus status);

    List<Invoice> findByTypeAndStatus(InvoiceType type, InvoiceStatus status);

    List<Invoice> findByIssueDateBetween(LocalDate from, LocalDate to);

    List<Invoice> findByPartnerNameContainingIgnoreCase(String keyword);

    @Query("SELECT i FROM Invoice i WHERE i.status = 'ISSUED' AND i.dueDate < :today")
    List<Invoice> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.type = :type AND i.status = 'PAID' AND i.issueDate BETWEEN :from AND :to")
    BigDecimal sumPaidByTypeAndDateRange(@Param("type") InvoiceType type,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);

    List<Invoice> findByOrderId(Long orderId);
}
