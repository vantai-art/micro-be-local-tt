package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.ActivityLogDto;
import com.rainbowforest.hrmservice.enums.ActionType;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogService {

    /**
     * Ghi log một hành động. Tự động lấy người thực hiện từ SecurityContext.
     */
    void log(ActionType action,
             String targetType, String targetId, String targetName,
             String oldValue, String newValue,
             String reason, String evidence);

    List<ActivityLogDto.Response> getRecent();

    List<ActivityLogDto.Response> search(String performedBy,
                                         ActionType action,
                                         String targetType,
                                         LocalDateTime from,
                                         LocalDateTime to,
                                         String keyword);

    List<ActivityLogDto.Response> getByTargetId(String targetId);
}
