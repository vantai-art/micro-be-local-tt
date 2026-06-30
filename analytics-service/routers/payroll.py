from fastapi import APIRouter, Query
from database import query_df
from datetime import date

router = APIRouter(prefix="/analytics/payroll", tags=["Payroll Analytics"])


@router.get("/trend")
def payroll_trend(year: int = Query(default=date.today().year)):
    """
    Tổng quỹ lương theo từng tháng trong năm.
    Dùng cho biểu đồ đường / cột.
    """
    df = query_df(
        """
        SELECT
            pay_month                          AS month,
            COUNT(*)                           AS employee_count,
            ROUND(SUM(net_salary), 0)          AS total_net,
            ROUND(AVG(net_salary), 0)          AS avg_net,
            ROUND(SUM(overtime_pay), 0)        AS total_overtime_pay,
            ROUND(SUM(bonus), 0)               AS total_bonus,
            ROUND(SUM(social_insurance + health_insurance), 0) AS total_insurance
        FROM payrolls
        WHERE pay_year = :year
        GROUP BY pay_month
        ORDER BY pay_month
        """,
        {"year": year},
    )
    return df.to_dict(orient="records")


@router.get("/by-department")
def by_department(
    month: int = Query(default=date.today().month),
    year: int = Query(default=date.today().year),
):
    """
    Lương trung bình / tổng quỹ lương theo phòng ban.
    Dùng cho biểu đồ cột nhóm.
    """
    df = query_df(
        """
        SELECT
            COALESCE(d.name, 'Chưa phân công') AS department,
            COUNT(p.id)                         AS employee_count,
            ROUND(AVG(p.basic_salary), 0)       AS avg_basic_salary,
            ROUND(AVG(p.net_salary), 0)         AS avg_net_salary,
            ROUND(SUM(p.net_salary), 0)         AS total_net_salary,
            ROUND(AVG(p.overtime_pay), 0)       AS avg_overtime_pay,
            ROUND(AVG(p.bonus), 0)              AS avg_bonus
        FROM payrolls p
        JOIN employees e ON p.employee_id = e.id
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE p.pay_month = :month AND p.pay_year = :year
        GROUP BY d.id
        ORDER BY total_net_salary DESC
        """,
        {"month": month, "year": year},
    )
    return df.to_dict(orient="records")


@router.get("/overtime-analysis")
def overtime_analysis(
    month: int = Query(default=date.today().month),
    year: int = Query(default=date.today().year),
    top_n: int = Query(default=10),
):
    """
    Top nhân viên có giờ làm thêm / chi phí overtime cao nhất tháng.
    """
    df = query_df(
        """
        SELECT
            e.employee_code,
            e.full_name,
            COALESCE(d.name, '-') AS department,
            p.overtime_hours,
            ROUND(p.overtime_pay, 0) AS overtime_pay,
            ROUND(p.net_salary, 0)   AS net_salary
        FROM payrolls p
        JOIN employees e ON p.employee_id = e.id
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE p.pay_month = :month AND p.pay_year = :year
          AND p.overtime_hours > 0
        ORDER BY p.overtime_hours DESC
        LIMIT :top_n
        """,
        {"month": month, "year": year, "top_n": top_n},
    )
    return df.to_dict(orient="records")


@router.get("/status-overview")
def status_overview(
    month: int = Query(default=date.today().month),
    year: int = Query(default=date.today().year),
):
    """
    Tỉ lệ bảng lương theo trạng thái: CALCULATED / APPROVED / PAID.
    Dùng cho biểu đồ tròn.
    """
    df = query_df(
        """
        SELECT status, COUNT(*) AS count, ROUND(SUM(net_salary), 0) AS total
        FROM payrolls
        WHERE pay_month = :month AND pay_year = :year
        GROUP BY status
        """,
        {"month": month, "year": year},
    )
    return df.to_dict(orient="records")
