package com.rainbowforest.hrmservice.enums;

public enum ActionType {
    // Nhân viên
    CREATE_EMPLOYEE,
    UPDATE_EMPLOYEE,
    DELETE_EMPLOYEE,
    CHANGE_EMPLOYEE_STATUS,

    // Chấm công
    CHECK_IN,
    CHECK_OUT,
    UPDATE_ATTENDANCE,
    CONFIRM_ATTENDANCE,

    // Nghỉ phép
    CREATE_LEAVE_REQUEST,
    APPROVE_LEAVE,
    REJECT_LEAVE,
    CANCEL_LEAVE,

    // Bảng lương
    CALCULATE_PAYROLL,
    APPROVE_PAYROLL,
    MARK_PAID_PAYROLL,

    // Phòng ban / Vị trí
    CREATE_DEPARTMENT,
    UPDATE_DEPARTMENT,
    DELETE_DEPARTMENT,
    CREATE_POSITION,
    UPDATE_POSITION,
    DELETE_POSITION
}
