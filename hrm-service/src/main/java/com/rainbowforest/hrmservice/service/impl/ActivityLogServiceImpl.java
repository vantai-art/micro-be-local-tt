package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.ActivityLog;
import com.rainbowforest.hrmservice.dto.ActivityLogDto;
import com.rainbowforest.hrmservice.enums.ActionType;
import com.rainbowforest.hrmservice.repository.ActivityLogRepository;
import com.rainbowforest.hrmservice.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm dd/MM");

    // Nhãn tiếng Việt cho từng ActionType
    private static final Map<ActionType, String> ACTION_LABELS = Map.ofEntries(
            Map.entry(ActionType.CREATE_EMPLOYEE, "Thêm nhân viên"),
            Map.entry(ActionType.UPDATE_EMPLOYEE, "Sửa nhân viên"),
            Map.entry(ActionType.DELETE_EMPLOYEE, "Xóa nhân viên"),
            Map.entry(ActionType.CHANGE_EMPLOYEE_STATUS, "Đổi trạng thái NV"),
            Map.entry(ActionType.CHECK_IN, "Check-in"),
            Map.entry(ActionType.CHECK_OUT, "Check-out"),
            Map.entry(ActionType.UPDATE_ATTENDANCE, "Sửa chấm công"),
            Map.entry(ActionType.CONFIRM_ATTENDANCE, "Xác nhận chấm công"),
            Map.entry(ActionType.CREATE_LEAVE_REQUEST, "Gửi đơn nghỉ phép"),
            Map.entry(ActionType.APPROVE_LEAVE, "Duyệt nghỉ phép"),
            Map.entry(ActionType.REJECT_LEAVE, "Từ chối nghỉ phép"),
            Map.entry(ActionType.CANCEL_LEAVE, "Hủy nghỉ phép"),
            Map.entry(ActionType.CALCULATE_PAYROLL, "Tính bảng lương"),
            Map.entry(ActionType.APPROVE_PAYROLL, "Duyệt bảng lương"),
            Map.entry(ActionType.MARK_PAID_PAYROLL, "Xác nhận đã trả lương"),
            Map.entry(ActionType.CREATE_DEPARTMENT, "Thêm phòng ban"),
            Map.entry(ActionType.UPDATE_DEPARTMENT, "Sửa phòng ban"),
            Map.entry(ActionType.DELETE_DEPARTMENT, "Xóa phòng ban"),
            Map.entry(ActionType.CREATE_POSITION, "Thêm chức vụ"),
            Map.entry(ActionType.UPDATE_POSITION, "Sửa chức vụ"),
            Map.entry(ActionType.DELETE_POSITION, "Xóa chức vụ")
    );

    @Override
    public void log(ActionType action,
                    String targetType, String targetId, String targetName,
                    String oldValue, String newValue,
                    String reason, String evidence) {
        try {
            String performer = resolveCurrentUser();
            LocalDateTime now = LocalDateTime.now();
            String summary = buildSummary(now, performer, action, targetName, oldValue, newValue, reason, evidence);

            ActivityLog entry = ActivityLog.builder()
                    .performedAt(now)
                    .performedBy(performer)
                    .action(action)
                    .targetType(targetType)
                    .targetId(targetId)
                    .targetName(targetName)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .reason(reason)
                    .evidence(evidence)
                    .summary(summary)
                    .build();

            activityLogRepository.save(entry);
            log.info("ActivityLog: {}", summary);
        } catch (Exception e) {
            // Log lỗi nhưng không làm gián đoạn nghiệp vụ chính
            log.error("Không thể ghi activity log: {}", e.getMessage());
        }
    }

    @Override
    public List<ActivityLogDto.Response> getRecent() {
        return activityLogRepository.findTop100ByOrderByPerformedAtDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogDto.Response> search(String performedBy,
                                                ActionType action,
                                                String targetType,
                                                LocalDateTime from,
                                                LocalDateTime to,
                                                String keyword) {
        return activityLogRepository.search(performedBy, action, targetType, from, to, keyword)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<ActivityLogDto.Response> getByTargetId(String targetId) {
        return activityLogRepository.findByTargetIdOrderByPerformedAtDesc(targetId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String resolveCurrentUser() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getName() != null && !auth.getName().equals("anonymousUser")) {
                return auth.getName();
            }
        } catch (Exception ignored) {
        }
        return "SYSTEM";
    }

    /**
     * Tạo dòng tóm tắt: "15:42 17/06 | admin | Sửa nhân viên Nguyễn A | Lương: 10tr → 12tr | Tăng lương | QĐ-001"
     */
    private String buildSummary(LocalDateTime at, String performer, ActionType action,
                                String targetName, String oldValue, String newValue,
                                String reason, String evidence) {
        StringBuilder sb = new StringBuilder();
        sb.append(at.format(TIME_FMT));
        sb.append(" | ").append(performer);
        sb.append(" | ").append(ACTION_LABELS.getOrDefault(action, action.name()));
        if (targetName != null && !targetName.isBlank()) {
            sb.append(" - ").append(targetName);
        }
        if (oldValue != null && newValue != null) {
            sb.append(" | ").append(oldValue).append(" → ").append(newValue);
        } else if (newValue != null) {
            sb.append(" | ").append(newValue);
        }
        if (reason != null && !reason.isBlank()) {
            sb.append(" | ").append(reason);
        }
        if (evidence != null && !evidence.isBlank()) {
            sb.append(" | ").append(evidence);
        }
        return sb.toString();
    }

    private ActivityLogDto.Response toResponse(ActivityLog l) {
        return ActivityLogDto.Response.builder()
                .id(l.getId())
                .performedAt(l.getPerformedAt())
                .performedBy(l.getPerformedBy())
                .action(l.getAction())
                .actionLabel(ACTION_LABELS.getOrDefault(l.getAction(), l.getAction().name()))
                .targetType(l.getTargetType())
                .targetId(l.getTargetId())
                .targetName(l.getTargetName())
                .oldValue(l.getOldValue())
                .newValue(l.getNewValue())
                .reason(l.getReason())
                .evidence(l.getEvidence())
                .ipAddress(l.getIpAddress())
                .summary(l.getSummary())
                .build();
    }
}
