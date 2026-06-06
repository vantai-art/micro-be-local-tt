package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.LeaveRequest;
import com.rainbowforest.hrmservice.enums.LeaveStatus;
import com.rainbowforest.hrmservice.enums.LeaveType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    List<LeaveRequest> findByEmployeeId(Long employeeId);
    List<LeaveRequest> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    List<LeaveRequest> findByStatus(LeaveStatus status);

    // Kiểm tra trùng lịch nghỉ
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employee.id = :empId " +
           "AND lr.status != 'REJECTED' AND lr.status != 'CANCELLED' " +
           "AND ((lr.startDate <= :endDate) AND (lr.endDate >= :startDate))")
    List<LeaveRequest> findOverlappingLeaves(@Param("empId") Long empId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Tổng ngày đã nghỉ phép năm trong năm
    @Query("SELECT COALESCE(SUM(lr.totalDays), 0) FROM LeaveRequest lr " +
           "WHERE lr.employee.id = :empId AND lr.leaveType = :type " +
           "AND lr.status = 'APPROVED' " +
           "AND YEAR(lr.startDate) = :year")
    Integer sumApprovedDaysByTypeAndYear(@Param("empId") Long empId,
                                          @Param("type") LeaveType type,
                                          @Param("year") int year);
}
