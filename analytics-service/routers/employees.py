from fastapi import APIRouter, Query
from database import query_df
from datetime import date

router = APIRouter(prefix="/analytics/employees", tags=["Employee Analytics"])


@router.get("/headcount-trend")
def headcount_trend(year: int = Query(default=date.today().year)):
    """
    Số nhân viên tuyển mới / nghỉ việc từng tháng trong năm.
    Dùng cho biểu đồ đường.
    """
    df = query_df(
        """
        SELECT
            MONTH(hire_date)  AS month,
            COUNT(*)          AS hired
        FROM employees
        WHERE YEAR(hire_date) = :year
        GROUP BY MONTH(hire_date)
        ORDER BY month
        """,
        {"year": year},
    )
    resigned_df = query_df(
        """
        SELECT
            MONTH(resign_date) AS month,
            COUNT(*)           AS resigned
        FROM employees
        WHERE YEAR(resign_date) = :year AND resign_date IS NOT NULL
        GROUP BY MONTH(resign_date)
        ORDER BY month
        """,
        {"year": year},
    )
    return {
        "hired": df.to_dict(orient="records"),
        "resigned": resigned_df.to_dict(orient="records"),
    }


@router.get("/by-department")
def by_department():
    """
    Phân bổ nhân viên theo phòng ban (chỉ ACTIVE).
    Dùng cho biểu đồ tròn.
    """
    df = query_df(
        """
        SELECT
            COALESCE(d.name, 'Chưa phân công') AS department,
            COUNT(*) AS count
        FROM employees e
        LEFT JOIN departments d ON e.department_id = d.id
        WHERE e.status = 'ACTIVE'
        GROUP BY d.id
        ORDER BY count DESC
        """
    )
    return df.to_dict(orient="records")


@router.get("/by-position")
def by_position():
    """
    Phân bổ nhân viên theo chức vụ (chỉ ACTIVE).
    """
    df = query_df(
        """
        SELECT
            COALESCE(p.name, 'Chưa phân công') AS position,
            COUNT(*) AS count
        FROM employees e
        LEFT JOIN positions p ON e.position_id = p.id
        WHERE e.status = 'ACTIVE'
        GROUP BY p.id
        ORDER BY count DESC
        """
    )
    return df.to_dict(orient="records")


@router.get("/salary-distribution")
def salary_distribution():
    """
    Phân phối lương cơ bản theo khoảng (histogram).
    Dùng cho biểu đồ histogram.
    """
    df = query_df(
        """
        SELECT
            CASE
                WHEN basic_salary < 5000000  THEN 'Dưới 5 triệu'
                WHEN basic_salary < 10000000 THEN '5-10 triệu'
                WHEN basic_salary < 15000000 THEN '10-15 triệu'
                WHEN basic_salary < 20000000 THEN '15-20 triệu'
                ELSE 'Trên 20 triệu'
            END AS salary_range,
            COUNT(*) AS count
        FROM employees
        WHERE status = 'ACTIVE' AND basic_salary IS NOT NULL
        GROUP BY salary_range
        ORDER BY MIN(basic_salary)
        """
    )
    return df.to_dict(orient="records")
