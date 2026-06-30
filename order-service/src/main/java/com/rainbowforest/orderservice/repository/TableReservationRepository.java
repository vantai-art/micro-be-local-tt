package com.rainbowforest.orderservice.repository;

import com.rainbowforest.orderservice.domain.TableReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TableReservationRepository extends JpaRepository<TableReservation, Long> {

    List<TableReservation> findByStatusOrderByArrivalTimeAsc(String status);

    List<TableReservation> findByUserId(Long userId);

    List<TableReservation> findByTableIdOrderByArrivalTimeAsc(Long tableId);

    /** Tìm đặt bàn theo khoảng thời gian (kiểm tra conflict) */
    @Query("SELECT r FROM TableReservation r WHERE r.table.id = :tableId " +
           "AND r.status NOT IN ('CANCELLED','COMPLETED') " +
           "AND r.arrivalTime BETWEEN :from AND :to")
    List<TableReservation> findConflicts(@Param("tableId") Long tableId,
                                         @Param("from") LocalDateTime from,
                                         @Param("to") LocalDateTime to);

    List<TableReservation> findByArrivalTimeBetweenOrderByArrivalTimeAsc(
            LocalDateTime from, LocalDateTime to);

    List<TableReservation> findAllByOrderByArrivalTimeDesc();
}
