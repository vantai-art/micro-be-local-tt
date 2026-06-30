package com.rainbowforest.orderservice.controller;

import com.rainbowforest.orderservice.domain.DiningTable;
import com.rainbowforest.orderservice.domain.TableReservation;
import com.rainbowforest.orderservice.repository.TableReservationRepository;
import com.rainbowforest.orderservice.service.DiningTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
public class TableReservationController {

    @Autowired
    private TableReservationRepository reservationRepo;

    @Autowired
    private DiningTableService tableService;

    @Autowired
    private SimpMessagingTemplate ws;

    // ──────────────────────────────────────────────────────────────
    // GET /reservations — tất cả (admin)
    // ──────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<TableReservation>> getAll() {
        return ResponseEntity.ok(reservationRepo.findAllByOrderByArrivalTimeDesc());
    }

    // ──────────────────────────────────────────────────────────────
    // GET /reservations/date?date=2025-06-15
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/date")
    public ResponseEntity<List<TableReservation>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to   = date.plusDays(1).atStartOfDay();
        return ResponseEntity.ok(
            reservationRepo.findByArrivalTimeBetweenOrderByArrivalTimeAsc(from, to));
    }

    // ──────────────────────────────────────────────────────────────
    // GET /reservations/user/{userId} — lịch sử của khách
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<TableReservation>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reservationRepo.findByUserId(userId));
    }

    // ──────────────────────────────────────────────────────────────
    // GET /reservations/{id}
    // ──────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<TableReservation> getById(@PathVariable Long id) {
        return reservationRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────
    // POST /reservations — khách đặt bàn
    // Body: { customerName, customerPhone, customerEmail, arrivalTime, partySize, notes, userId? }
    // ──────────────────────────────────────────────────────────────
    @PostMapping
    public ResponseEntity<?> create(@RequestBody TableReservation body) {
        if (body.getCustomerName() == null || body.getCustomerName().isBlank())
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng nhập tên khách"));
        if (body.getArrivalTime() == null)
            return ResponseEntity.badRequest().body(Map.of("error", "Vui lòng chọn thời gian đến"));
        if (body.getArrivalTime().isBefore(LocalDateTime.now()))
            return ResponseEntity.badRequest().body(Map.of("error", "Thời gian đến phải ở tương lai"));
        if (body.getPartySize() == null || body.getPartySize() < 1)
            body.setPartySize(1);

        body.setStatus("PENDING");
        body.setTable(null); // Admin mới assign bàn

        TableReservation saved = reservationRepo.save(body);

        // Broadcast realtime cho admin
        ws.convertAndSend("/topic/reservations",
            Map.of("type", "reservation:new", "data", saved));

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /reservations/{id} — admin sửa (assign bàn, đổi giờ...)
    // ──────────────────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return reservationRepo.findById(id).map(res -> {
            if (body.containsKey("customerName"))
                res.setCustomerName((String) body.get("customerName"));
            if (body.containsKey("customerPhone"))
                res.setCustomerPhone((String) body.get("customerPhone"));
            if (body.containsKey("customerEmail"))
                res.setCustomerEmail((String) body.get("customerEmail"));
            if (body.containsKey("notes"))
                res.setNotes((String) body.get("notes"));
            if (body.containsKey("partySize"))
                res.setPartySize(Integer.valueOf(body.get("partySize").toString()));
            if (body.containsKey("arrivalTime")) {
                res.setArrivalTime(LocalDateTime.parse((String) body.get("arrivalTime")));
            }
            // Assign bàn
            if (body.containsKey("tableId")) {
                Object tid = body.get("tableId");
                if (tid == null) {
                    res.setTable(null);
                } else {
                    Long tableId = Long.valueOf(tid.toString());
                    DiningTable t = tableService.getTableById(tableId);
                    if (t == null) return ResponseEntity.badRequest()
                            .body((Object) Map.of("error", "Bàn không tồn tại"));
                    res.setTable(t);
                }
            }

            TableReservation saved = reservationRepo.save(res);
            ws.convertAndSend("/topic/reservations",
                Map.of("type", "reservation:updated", "data", saved));
            return ResponseEntity.ok((Object) saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /reservations/{id}/confirm — admin xác nhận + assign bàn
    // Body: { tableId: Long }
    // ──────────────────────────────────────────────────────────────
    @PutMapping("/{id}/confirm")
    public ResponseEntity<?> confirm(@PathVariable Long id,
                                      @RequestBody(required = false) Map<String, Object> body) {
        return reservationRepo.findById(id).map(res -> {
            if (body != null && body.containsKey("tableId")) {
                Long tableId = Long.valueOf(body.get("tableId").toString());
                DiningTable t = tableService.getTableById(tableId);
                if (t == null) return ResponseEntity.badRequest()
                        .body((Object) Map.of("error", "Bàn không tồn tại"));
                // Đánh dấu bàn RESERVED
                tableService.updateTableStatus(tableId, "RESERVED");
                res.setTable(t);
                // Broadcast bàn đổi trạng thái
                ws.convertAndSend("/topic/table/" + tableId,
                    Map.of("type", "table:status_changed", "tableId", tableId, "data", t));
            }
            res.setStatus("CONFIRMED");
            TableReservation saved = reservationRepo.save(res);
            ws.convertAndSend("/topic/reservations",
                Map.of("type", "reservation:confirmed", "data", saved));
            return ResponseEntity.ok((Object) saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /reservations/{id}/seat — khách đến, đổi bàn → OCCUPIED
    // ──────────────────────────────────────────────────────────────
    @PutMapping("/{id}/seat")
    public ResponseEntity<?> seat(@PathVariable Long id) {
        return reservationRepo.findById(id).map(res -> {
            res.setStatus("SEATED");
            if (res.getTable() != null) {
                tableService.updateTableStatus(res.getTable().getId(), "OCCUPIED");
                ws.convertAndSend("/topic/table/" + res.getTable().getId(),
                    Map.of("type", "table:status_changed",
                           "tableId", res.getTable().getId(),
                           "data", res.getTable()));
            }
            TableReservation saved = reservationRepo.save(res);
            ws.convertAndSend("/topic/reservations",
                Map.of("type", "reservation:seated", "data", saved));
            return ResponseEntity.ok((Object) saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────
    // PUT /reservations/{id}/cancel
    // ──────────────────────────────────────────────────────────────
    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long id) {
        return reservationRepo.findById(id).map(res -> {
            // Trả bàn về FREE nếu đã assign
            if (res.getTable() != null && "CONFIRMED".equals(res.getStatus())) {
                tableService.updateTableStatus(res.getTable().getId(), "FREE");
                ws.convertAndSend("/topic/table/" + res.getTable().getId(),
                    Map.of("type", "table:status_changed",
                           "tableId", res.getTable().getId(),
                           "data", res.getTable()));
            }
            res.setStatus("CANCELLED");
            TableReservation saved = reservationRepo.save(res);
            ws.convertAndSend("/topic/reservations",
                Map.of("type", "reservation:cancelled", "data", saved));
            return ResponseEntity.ok((Object) saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // ──────────────────────────────────────────────────────────────
    // DELETE /reservations/{id} — xóa hẳn (admin)
    // ──────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!reservationRepo.existsById(id))
            return ResponseEntity.notFound().build();
        reservationRepo.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
