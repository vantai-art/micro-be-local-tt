package com.rainbowforest.hrmservice.domain;

import com.rainbowforest.hrmservice.enums.ActionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs", indexes = {
        @Index(name = "idx_log_performed_by", columnList = "performed_by"),
        @Index(name = "idx_log_action", columnList = "action"),
        @Index(name = "idx_log_performed_at", columnList = "performed_at"),
        @Index(name = "idx_log_target_type", columnList = "target_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Thời gian thực hiện
    @Column(name = "performed_at", nullable = false)
    private LocalDateTime performedAt;

    // Người thực hiện (username hoặc employee code)
    @Column(name = "performed_by", nullable = false, length = 100)
    private String performedBy;

    // Hành động
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 50)
    private ActionType action;

    // Loại đối tượng bị tác động (EMPLOYEE, ATTENDANCE, LEAVE_REQUEST, PAYROLL...)
    @Column(name = "target_type", length = 50)
    private String targetType;

    // ID đối tượng (mã nhân viên, ID đơn...)
    @Column(name = "target_id", length = 100)
    private String targetId;

    // Tên dễ đọc của đối tượng
    @Column(name = "target_name", length = 200)
    private String targetName;

    // Giá trị trước thay đổi
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    // Giá trị sau thay đổi
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    // Lý do thực hiện hành động
    @Column(name = "reason", length = 500)
    private String reason;

    // Bằng chứng (số phiếu, link hệ thống, camera ref...)
    @Column(name = "evidence", length = 500)
    private String evidence;

    // IP người thực hiện
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    // Tóm tắt dạng: "15:42 | NV001 | Sửa nhân viên Nguyễn A | Lương: 10tr → 12tr | Tăng lương | Quyết định QĐ-001"
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;
}
