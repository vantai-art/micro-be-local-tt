package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.Payroll;
import com.rainbowforest.hrmservice.enums.PayrollStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PayrollRepository extends JpaRepository<Payroll, Long> {

    Optional<Payroll> findByEmployeeIdAndPayMonthAndPayYear(Long employeeId, int month, int year);

    List<Payroll> findByPayMonthAndPayYear(int month, int year);
    List<Payroll> findByEmployeeId(Long employeeId);
    List<Payroll> findByPayMonthAndPayYearAndStatus(int month, int year, PayrollStatus status);

    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM Payroll p WHERE p.payMonth = :month AND p.payYear = :year AND p.status = 'PAID'")
    BigDecimal sumNetSalaryByMonthAndYear(@Param("month") int month, @Param("year") int year);
}
