package com.rainbowforest.hrmservice.repository;

import com.rainbowforest.hrmservice.domain.ActivityLog;
import com.rainbowforest.hrmservice.enums.ActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    List<ActivityLog> findByPerformedByOrderByPerformedAtDesc(String performedBy);

    List<ActivityLog> findByActionOrderByPerformedAtDesc(ActionType action);

    List<ActivityLog> findByTargetTypeOrderByPerformedAtDesc(String targetType);

    List<ActivityLog> findByTargetIdOrderByPerformedAtDesc(String targetId);

    List<ActivityLog> findByPerformedAtBetweenOrderByPerformedAtDesc(
            LocalDateTime from, LocalDateTime to);

    @Query("""
            SELECT l FROM ActivityLog l
            WHERE (:performedBy IS NULL OR l.performedBy = :performedBy)
              AND (:action IS NULL OR l.action = :action)
              AND (:targetType IS NULL OR l.targetType = :targetType)
              AND (:from IS NULL OR l.performedAt >= :from)
              AND (:to IS NULL OR l.performedAt <= :to)
              AND (:keyword IS NULL OR
                   LOWER(l.summary) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
                   LOWER(l.targetName) LIKE LOWER(CONCAT('%', :keyword, '%')))
            ORDER BY l.performedAt DESC
            """)
    List<ActivityLog> search(
            @Param("performedBy") String performedBy,
            @Param("action") ActionType action,
            @Param("targetType") String targetType,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("keyword") String keyword);

    List<ActivityLog> findTop100ByOrderByPerformedAtDesc();
}
