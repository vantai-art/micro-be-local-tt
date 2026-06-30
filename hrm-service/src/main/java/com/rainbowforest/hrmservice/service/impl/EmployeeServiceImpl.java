package com.rainbowforest.hrmservice.service.impl;

import com.rainbowforest.hrmservice.domain.Branch;
import com.rainbowforest.hrmservice.domain.Department;
import com.rainbowforest.hrmservice.domain.Employee;
import com.rainbowforest.hrmservice.domain.Position;
import com.rainbowforest.hrmservice.dto.EmployeeDto;
import com.rainbowforest.hrmservice.enums.ActionType;
import com.rainbowforest.hrmservice.enums.EmployeeStatus;
import com.rainbowforest.hrmservice.repository.BranchRepository;
import com.rainbowforest.hrmservice.repository.DepartmentRepository;
import com.rainbowforest.hrmservice.repository.EmployeeRepository;
import com.rainbowforest.hrmservice.repository.PositionRepository;
import com.rainbowforest.hrmservice.service.ActivityLogService;
import com.rainbowforest.hrmservice.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final BranchRepository branchRepository;
    private final ActivityLogService activityLogService;

    @Override
    @Transactional
    public EmployeeDto.Response create(EmployeeDto.Request request) {
        if (request.getEmail() != null && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
        }

        Department department = null;
        if (request.getDepartmentId() != null) {
            department = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new NoSuchElementException(
                            "Không tìm thấy phòng ban id: " + request.getDepartmentId()));
        }

        Position position = null;
        if (request.getPositionId() != null) {
            position = positionRepository.findById(request.getPositionId())
                    .orElseThrow(
                            () -> new NoSuchElementException("Không tìm thấy vị trí id: " + request.getPositionId()));
        }

        Branch branch = null;
        if (request.getBranchId() != null) {
            branch = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi nhánh id: " + request.getBranchId()));
        }

        Employee employee = Employee.builder()
                .employeeCode(generateEmployeeCode())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .identityCard(request.getIdentityCard())
                .hireDate(request.getHireDate())
                .status(EmployeeStatus.ACTIVE)
                .basicSalary(request.getBasicSalary())
                .allowance(request.getAllowance())
                .department(department)
                .position(position)
                .branch(branch)
                .userId(request.getUserId())
                .annualLeaveDays(request.getAnnualLeaveDays() != null ? request.getAnnualLeaveDays() : 12)
                .avatarUrl(request.getAvatarUrl())
                .note(request.getNote())
                .build();

        Employee saved = employeeRepository.save(employee);
        log.info("Tạo nhân viên mới: {} - {}", saved.getEmployeeCode(), saved.getFullName());
        activityLogService.log(ActionType.CREATE_EMPLOYEE,
                "EMPLOYEE", saved.getEmployeeCode(), saved.getFullName(),
                null, summarizeEmployee(saved),
                request.getReason(), request.getEvidence());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public EmployeeDto.Response update(Long id, EmployeeDto.Request request) {
        Employee employee = findEmployeeById(id);

        if (request.getEmail() != null && !request.getEmail().equals(employee.getEmail())
                && employeeRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.getEmail());
        }

        String oldSummary = summarizeEmployee(employee);

        employee.setFullName(request.getFullName());
        employee.setEmail(request.getEmail());
        employee.setPhone(request.getPhone());
        employee.setGender(request.getGender());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setAddress(request.getAddress());
        employee.setIdentityCard(request.getIdentityCard());
        employee.setBasicSalary(request.getBasicSalary());
        employee.setAllowance(request.getAllowance());
        employee.setAvatarUrl(request.getAvatarUrl());
        employee.setNote(request.getNote());

        if (request.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(request.getDepartmentId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy phòng ban"));
            employee.setDepartment(dept);
        }

        if (request.getPositionId() != null) {
            Position pos = positionRepository.findById(request.getPositionId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy vị trí"));
            employee.setPosition(pos);
        }

        if (request.getBranchId() != null) {
            Branch br = branchRepository.findById(request.getBranchId())
                    .orElseThrow(() -> new NoSuchElementException("Không tìm thấy chi nhánh id: " + request.getBranchId()));
            employee.setBranch(br);
        }

        if (request.getAnnualLeaveDays() != null) {
            employee.setAnnualLeaveDays(request.getAnnualLeaveDays());
        }

        Employee saved = employeeRepository.save(employee);
        activityLogService.log(ActionType.UPDATE_EMPLOYEE,
                "EMPLOYEE", saved.getEmployeeCode(), saved.getFullName(),
                oldSummary, summarizeEmployee(saved),
                request.getReason(), request.getEvidence());
        return toResponse(saved);
    }

    @Override
    public EmployeeDto.Response getById(Long id) {
        return toResponse(findEmployeeById(id));
    }

    @Override
    public EmployeeDto.Response getByEmployeeCode(String code) {
        return toResponse(employeeRepository.findByEmployeeCode(code)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên: " + code)));
    }

    @Override
    public List<EmployeeDto.Response> getAll() {
        return employeeRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDto.Response> getByDepartment(Long departmentId) {
        return employeeRepository.findByDepartmentId(departmentId).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDto.Response> getByStatus(EmployeeStatus status) {
        return employeeRepository.findByStatus(status).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<EmployeeDto.Response> search(String keyword) {
        return employeeRepository.searchByKeyword(keyword).stream()
                .map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EmployeeDto.Response changeStatus(Long id, EmployeeStatus status) {
        Employee employee = findEmployeeById(id);
        String oldStatus = employee.getStatus().name();
        employee.setStatus(status);
        if (status == EmployeeStatus.RESIGNED || status == EmployeeStatus.TERMINATED) {
            employee.setResignDate(LocalDate.now());
        }
        Employee saved = employeeRepository.save(employee);
        activityLogService.log(ActionType.CHANGE_EMPLOYEE_STATUS,
                "EMPLOYEE", saved.getEmployeeCode(), saved.getFullName(),
                oldStatus, status.name(), null, null);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Employee employee = findEmployeeById(id);
        String code = employee.getEmployeeCode();
        String name = employee.getFullName();
        employee.setStatus(EmployeeStatus.TERMINATED);
        employeeRepository.save(employee);
        activityLogService.log(ActionType.DELETE_EMPLOYEE,
                "EMPLOYEE", code, name,
                "ACTIVE", "TERMINATED", null, null);
    }

    @Override
    @Transactional
    public void forceDelete(Long id) {
        Employee employee = findEmployeeById(id);
        String code = employee.getEmployeeCode();
        String name = employee.getFullName();
        employeeRepository.delete(employee);
        activityLogService.log(ActionType.DELETE_EMPLOYEE,
                "EMPLOYEE", code, name,
                summarizeEmployee(employee), "XÓA VĨNH VIỄN", null, null);
    }

    // ---- Helpers ----

    private Employee findEmployeeById(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên id: " + id));
    }

    private String summarizeEmployee(Employee e) {
        return String.format("Lương: %s | Phụ cấp: %s | Phòng ban: %s | Chức vụ: %s | Trạng thái: %s",
                e.getBasicSalary(),
                e.getAllowance(),
                e.getDepartment() != null ? e.getDepartment().getName() : "-",
                e.getPosition() != null ? e.getPosition().getName() : "-",
                e.getStatus());
    }

    private String generateEmployeeCode() {
        String prefix = "NV" + DateTimeFormatter.ofPattern("yy").format(LocalDate.now());
        long count = employeeRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }

    @Override
    public EmployeeDto.Response getByUserId(Long userId) {
        return toResponse(employeeRepository.findByUserId(userId)
                .orElseThrow(() -> new NoSuchElementException("Không tìm thấy nhân viên với userId: " + userId)));
    }

    private EmployeeDto.Response toResponse(Employee e) {
        var b = e.getBranch();
        return EmployeeDto.Response.builder()
                .id(e.getId())
                .employeeCode(e.getEmployeeCode())
                .fullName(e.getFullName())
                .email(e.getEmail())
                .phone(e.getPhone())
                .gender(e.getGender())
                .dateOfBirth(e.getDateOfBirth())
                .address(e.getAddress())
                .identityCard(e.getIdentityCard())
                .hireDate(e.getHireDate())
                .resignDate(e.getResignDate())
                .status(e.getStatus())
                .basicSalary(e.getBasicSalary())
                .allowance(e.getAllowance())
                .departmentId(e.getDepartment() != null ? e.getDepartment().getId() : null)
                .departmentName(e.getDepartment() != null ? e.getDepartment().getName() : null)
                .positionId(e.getPosition() != null ? e.getPosition().getId() : null)
                .positionName(e.getPosition() != null ? e.getPosition().getName() : null)
                .branchId(b != null ? b.getId() : null)
                .branchName(b != null ? b.getName() : null)
                .branchSsid(b != null ? b.getSsid() : null)
                .branchBssid(b != null ? b.getBssid() : null)
                .branchLatitude(b != null ? b.getLatitude() : null)
                .branchLongitude(b != null ? b.getLongitude() : null)
                .branchRadiusMeters(b != null ? b.getRadiusMeters() : null)
                .branchIsDemo(b != null ? b.getIsDemo() : null)
                .userId(e.getUserId())
                .annualLeaveDays(e.getAnnualLeaveDays())
                .avatarUrl(e.getAvatarUrl())
                .note(e.getNote())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}