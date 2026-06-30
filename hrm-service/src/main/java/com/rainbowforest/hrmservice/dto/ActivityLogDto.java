package com.rainbowforest.hrmservice.dto;

import com.rainbowforest.hrmservice.enums.ActionType;
import lombok.*;

import java.time.LocalDateTime;

public class ActivityLogDto {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Response {
        private Long id;
        private LocalDateTime performedAt;
        private String performedBy;
        private ActionType action;
        private String actionLabel;      // Nhãn tiếng Việt
        private String targetType;
        private String targetId;
        private String targetName;
        private String oldValue;
        private String newValue;
        private String reason;
        private String evidence;
        private String ipAddress;
        private String summary;          // Dòng tóm tắt đầy đủ
    }
}
