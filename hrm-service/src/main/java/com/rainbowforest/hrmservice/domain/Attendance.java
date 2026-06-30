package com.rainbowforest.hrmservice.domain;

import com.rainbowforest.hrmservice.enums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "attendances",
       uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "attendance_date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Column(name = "check_in")
    private LocalTime checkIn;

    @Column(name = "check_out")
    private LocalTime checkOut;

    // Số giờ làm việc thực tế
    @Column(name = "worked_hours")
    private Double workedHours;

    // Số giờ làm thêm (overtime)
    @Column(name = "overtime_hours")
    private Double overtimeHours = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AttendanceStatus status = AttendanceStatus.PRESENT;

    @Column(name = "note", length = 255)
    private String note;

    // Ca làm việc
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    // Tọa độ GPS khi check-in
    @Column(name = "check_in_latitude")
    private Double checkInLatitude;

    @Column(name = "check_in_longitude")
    private Double checkInLongitude;

    // BSSID của WiFi dùng khi check-in
    @Column(name = "bssid_used", length = 50)
    private String bssidUsed;

    // Chế độ demo (true) hay production (false)
    @Column(name = "is_demo", columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isDemo = false;

    // Người xác nhận (manager id)
    @Column(name = "confirmed_by")
    private Long confirmedBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
