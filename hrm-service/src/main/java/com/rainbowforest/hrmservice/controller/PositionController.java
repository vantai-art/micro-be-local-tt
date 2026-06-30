package com.rainbowforest.hrmservice.controller;

import com.rainbowforest.hrmservice.domain.Department;
import com.rainbowforest.hrmservice.domain.Position;
import com.rainbowforest.hrmservice.repository.DepartmentRepository;
import com.rainbowforest.hrmservice.repository.PositionRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/hrm/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionRepository positionRepository;
    private final DepartmentRepository departmentRepository;

    @PostMapping
    public ResponseEntity<Position> create(@Valid @RequestBody Position position) {
        // Gán department nếu có departmentId
        if (position.getDepartment() != null && position.getDepartment().getId() != null) {
            Department dept = departmentRepository.findById(position.getDepartment().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Không tìm thấy phòng ban id: " + position.getDepartment().getId()));
            position.setDepartment(dept);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(positionRepository.save(position));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Position> update(@PathVariable Long id, @Valid @RequestBody Position body) {
        Position pos = positionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chức vụ id: " + id));
        pos.setName(body.getName());
        pos.setCode(body.getCode());
        pos.setDescription(body.getDescription());
        pos.setBaseSalary(body.getBaseSalary());
        pos.setActive(body.getActive());

        if (body.getDepartment() != null && body.getDepartment().getId() != null) {
            Department dept = departmentRepository.findById(body.getDepartment().getId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Không tìm thấy phòng ban id: " + body.getDepartment().getId()));
            pos.setDepartment(dept);
        } else {
            pos.setDepartment(null);
        }

        return ResponseEntity.ok(positionRepository.save(pos));
    }

    @GetMapping
    public ResponseEntity<List<Position>> getAll() {
        return ResponseEntity.ok(positionRepository.findAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<Position>> getActive() {
        return ResponseEntity.ok(positionRepository.findByActiveTrue());
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<Position>> getByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(positionRepository.findByDepartmentId(departmentId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Position> getById(@PathVariable Long id) {
        return ResponseEntity.ok(positionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chức vụ id: " + id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Position pos = positionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chức vụ id: " + id));
        pos.setActive(false);
        positionRepository.save(pos);
        return ResponseEntity.noContent().build();
    }
}