package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.Attendance;
import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(Long employeeId, LocalDate from, LocalDate to);

    List<Attendance> findByAttendanceDate(LocalDate date);

    @Query("SELECT a FROM Attendance a WHERE a.employee.department.id = :deptId AND a.attendanceDate = :date")
    List<Attendance> findByDepartmentAndDate(@Param("deptId") Long deptId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.employee.id = :empId " +
           "AND a.attendanceDate BETWEEN :from AND :to AND a.status = :status")
    long countByEmployeeAndDateRangeAndStatus(@Param("empId") Long empId,
                                              @Param("from") LocalDate from,
                                              @Param("to") LocalDate to,
                                              @Param("status") AttendanceStatus status);

    @Query("SELECT COALESCE(SUM(a.workedHours), 0) FROM Attendance a WHERE a.employee.id = :empId " +
           "AND a.attendanceDate BETWEEN :from AND :to")
    Double sumWorkedHoursByEmployeeAndDateRange(@Param("empId") Long empId,
                                                @Param("from") LocalDate from,
                                                @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(a.overtimeHours), 0) FROM Attendance a WHERE a.employee.id = :empId " +
           "AND a.attendanceDate BETWEEN :from AND :to")
    Double sumOvertimeHoursByEmployeeAndDateRange(@Param("empId") Long empId,
                                                   @Param("from") LocalDate from,
                                                   @Param("to") LocalDate to);
}
