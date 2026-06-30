package com.rainbowforest.hrmservice.service;

import com.rainbowforest.hrmservice.dto.EmployeeDto;
import com.rainbowforest.hrmservice.enums.EmployeeStatus;

import java.util.List;

public interface EmployeeService {
    EmployeeDto.Response create(EmployeeDto.Request request);

    EmployeeDto.Response update(Long id, EmployeeDto.Request request);

    EmployeeDto.Response getById(Long id);

    EmployeeDto.Response getByEmployeeCode(String code);

    List<EmployeeDto.Response> getAll();

    List<EmployeeDto.Response> getByDepartment(Long departmentId);

    List<EmployeeDto.Response> getByStatus(EmployeeStatus status);

    List<EmployeeDto.Response> search(String keyword);

    EmployeeDto.Response changeStatus(Long id, EmployeeStatus status);

    EmployeeDto.Response getByUserId(Long userId);

    void delete(Long id);

    void forceDelete(Long id);
}