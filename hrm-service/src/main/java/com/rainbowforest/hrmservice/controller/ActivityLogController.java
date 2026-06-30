package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.dto.ActivityLogDto;
import com.rainbowforest.hrmservice.enums.ActionType;
import com.rainbowforest.hrmservice.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/hrm/activity-logs")
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService activityLogService;

    /**
     * Lấy 100 log gần nhất — màn hình dashboard admin
     */
    @GetMapping
    public ResponseEntity<List<ActivityLogDto.Response>> getRecent() {
        return ResponseEntity.ok(activityLogService.getRecent());
    }

    /**
     * Tìm kiếm log theo nhiều tiêu chí:
     * - performedBy: tên người thực hiện
     * - action: loại hành động (CREATE_EMPLOYEE, CHECK_IN...)
     * - targetType: EMPLOYEE | ATTENDANCE | LEAVE_REQUEST | PAYROLL
     * - from / to: khoảng thời gian (ISO 8601)
     * - keyword: từ khóa tìm trong summary và tên đối tượng
     *
     * Ví dụ: GET /hrm/activity-logs/search?performedBy=admin&from=2026-06-01T00:00:00
     */
    @GetMapping("/search")
    public ResponseEntity<List<ActivityLogDto.Response>> search(
            @RequestParam(required = false) String performedBy,
            @RequestParam(required = false) ActionType action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(
                activityLogService.search(performedBy, action, targetType, from, to, keyword));
    }

    /**
     * Lấy toàn bộ lịch sử của một đối tượng cụ thể (VD: mã NV, ID đơn...)
     * Ví dụ: GET /hrm/activity-logs/target/NV260001
     */
    @GetMapping("/target/{targetId}")
    public ResponseEntity<List<ActivityLogDto.Response>> getByTarget(
            @PathVariable String targetId) {
        return ResponseEntity.ok(activityLogService.getByTargetId(targetId));
    }
}
