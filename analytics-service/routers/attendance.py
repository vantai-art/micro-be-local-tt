from fastapi import APIRouter, Query
from database import query_df
from datetime import date
from typing import Optional

router = APIRouter(prefix="/analytics/attendance", tags=["Attendance Analytics"])


@router.get("/summary")
def attendance_summary(
    month: int = Query(default=date.today().month),
    year: int = Query(default=date.today().year),
    department_id: Optional[int] = None,
):
    """
    Tổng hợp chấm công theo phòng ban trong tháng.
    Trả về: số ngày có mặt, vắng, muộn, nghỉ phép — theo từng phòng ban.
    """
    dept_filter = "AND e.department_id = :dept_id" if department_id else ""
    df = query_df(
        f"""
        SELECT
            d.name AS department,
            e.employee_code,
            e.full_name,
            SUM(a.status = 'PRESENT') AS present,
            SUM(a.status = 'LATE')    AS late,
            SUM(a.status = 'ABSENT')  AS absent,
            SUM(a.status = 'ON_LEAVE') AS on_leave,
            SUM(a.status = 'HALF_DAY') AS half_day,
            ROUND(AVG(a.worked_hours), 2) AS avg_worked_hours,
            ROUND(SUM(a.overtime_hours), 2) AS total_overtime
        FROM attendances a
        JOIN employees e ON a.employee_id = e.id
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE MONTH(a.attendance_date) = :month AND YEAR(a.attendance_date) = :year
          {dept_filter}
        GROUP BY e.id
        ORDER BY d.name, e.full_name
        """,
        {"month": month, "year": year, "dept_id": department_id},
    )
    return df.to_dict(orient="records")


@router.get("/department-summary")
def department_summary(
    month: int = Query(default=date.today().month),
    year: int = Query(default=date.today().year),
):
    """
    Tỉ lệ có mặt / vắng / muộn theo từng phòng ban — dùng cho biểu đồ cột.
    """
    df = query_df(
        """
        SELECT
            COALESCE(d.name, 'Chưa phân công') AS department,
            COUNT(DISTINCT e.id)                AS employee_count,
            SUM(a.status = 'PRESENT')           AS present,
            SUM(a.status = 'LATE')              AS late,
            SUM(a.status = 'ABSENT')            AS absent,
            SUM(a.status = 'ON_LEAVE')          AS on_leave,
            ROUND(
                (SUM(a.status = 'PRESENT') + SUM(a.status = 'LATE') + SUM(a.status = 'ON_LEAVE'))
                / NULLIF(COUNT(*), 0) * 100, 1
            ) AS attendance_rate_pct
        FROM attendances a
        JOIN employees e ON a.employee_id = e.id
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE MONTH(a.attendance_date) = :month AND YEAR(a.attendance_date) = :year
        GROUP BY d.id
        ORDER BY attendance_rate_pct DESC
        """,
        {"month": month, "year": year},
    )
    return df.to_dict(orient="records")


@router.get("/late-trend")
def late_trend(
    employee_code: Optional[str] = None,
    months: int = Query(default=3, ge=1, le=12),
):
    """
    Xu hướng đi muộn trong N tháng gần nhất.
    Nếu không truyền employee_code thì lấy toàn bộ công ty.
    Dùng cho biểu đồ đường.
    """
    emp_filter = "AND e.employee_code = :code" if employee_code else ""
    df = query_df(
        f"""
        SELECT
            DATE_FORMAT(a.attendance_date, '%Y-%m') AS month_label,
            COUNT(*) AS late_count
        FROM attendances a
        JOIN employees e ON a.employee_id = e.id
        WHERE a.status = 'LATE'
          AND a.attendance_date >= DATE_SUB(CURDATE(), INTERVAL :months MONTH)
          {emp_filter}
        GROUP BY month_label
        ORDER BY month_label
        """,
        {"months": months, "code": employee_code},
    )
    return df.to_dict(orient="records")


@router.get("/heatmap")
def attendance_heatmap(
    from_date: str = Query(default=str(date.today().replace(day=1))),
    to_date: str = Query(default=str(date.today())),
):
    """
    Heatmap chấm công theo ngày trong tuần × trạng thái.
    Trả về ma trận: {dayOfWeek, status, count} dùng cho chart heatmap.
    """
    df = query_df(
        """
        SELECT
            DAYOFWEEK(attendance_date) AS dow,   -- 1=CN, 2=T2, ..., 7=T7
            DAYNAME(attendance_date)   AS day_name,
            status,
            COUNT(*)                   AS count
        FROM attendances
        WHERE attendance_date BETWEEN :from_date AND :to_date
        GROUP BY dow, status
        ORDER BY dow
        """,
        {"from_date": from_date, "to_date": to_date},
    )
    return df.to_dict(orient="records")


@router.get("/risk-employees")
def risk_employees(
    month: int = Query(default=date.today().month),
    year: int = Query(default=date.today().year),
    late_threshold: int = Query(default=3, description="Số ngày muộn tối thiểu để cảnh báo"),
    absent_threshold: int = Query(default=2, description="Số ngày vắng tối thiểu để cảnh báo"),
):
    """
    Danh sách nhân viên có nguy cơ: đi muộn nhiều hoặc vắng mặt nhiều trong tháng.
    """
    df = query_df(
        """
        SELECT
            e.employee_code,
            e.full_name,
            COALESCE(d.name, '-') AS department,
            SUM(a.status = 'LATE')   AS late_days,
            SUM(a.status = 'ABSENT') AS absent_days,
            SUM(a.status = 'PRESENT') AS present_days,
            ROUND(SUM(a.worked_hours), 2) AS total_worked_hours
        FROM attendances a
        JOIN employees e ON a.employee_id = e.id
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE MONTH(a.attendance_date) = :month AND YEAR(a.attendance_date) = :year
        GROUP BY e.id
        HAVING late_days >= :late OR absent_days >= :absent
        ORDER BY absent_days DESC, late_days DESC
        """,
        {"month": month, "year": year, "late": late_threshold, "absent": absent_threshold},
    )
    return df.to_dict(orient="records")
