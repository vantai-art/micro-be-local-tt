from fastapi import APIRouter
from database import query_df, scalar
from datetime import date

router = APIRouter(prefix="/analytics/dashboard", tags=["Dashboard"])


@router.get("")
def get_dashboard():
    """
    Tổng hợp toàn bộ số liệu cho dashboard admin.
    """
    today = date.today()
    month, year = today.month, today.year

    # ── Nhân viên ────────────────────────────────────────────────────
    emp_df = query_df("SELECT status, COUNT(*) AS cnt FROM employees GROUP BY status")
    emp_stats = {row["status"]: int(row["cnt"]) for _, row in emp_df.iterrows()}
    total_active = emp_stats.get("ACTIVE", 0)

    # ── Chấm công tháng này ──────────────────────────────────────────
    att_df = query_df(
        """
        SELECT status, COUNT(*) AS cnt FROM attendances
        WHERE MONTH(attendance_date) = :month AND YEAR(attendance_date) = :year
        GROUP BY status
        """,
        {"month": month, "year": year},
    )
    att_stats = {row["status"]: int(row["cnt"]) for _, row in att_df.iterrows()}
    total_days = sum(att_stats.values()) or 1
    attendance_rate = round(
        (att_stats.get("PRESENT", 0) + att_stats.get("LATE", 0) + att_stats.get("ON_LEAVE", 0))
        / total_days * 100, 1
    )

    # ── Đơn nghỉ phép đang chờ ───────────────────────────────────────
    pending_leaves = scalar(
        "SELECT COUNT(*) FROM leave_requests WHERE status = 'PENDING'"
    ) or 0

    # ── Bảng lương tháng này ─────────────────────────────────────────
    payroll_df = query_df(
        """
        SELECT status, COUNT(*) AS cnt,
               COALESCE(SUM(net_salary), 0) AS total
        FROM payrolls
        WHERE pay_month = :month AND pay_year = :year
        GROUP BY status
        """,
        {"month": month, "year": year},
    )
    payroll_summary = {
        row["status"]: {"count": int(row["cnt"]), "total": float(row["total"])}
        for _, row in payroll_df.iterrows()
    }

    # ── Hoạt động 7 ngày gần nhất ───────────────────────────────────
    activity_df = query_df(
        """
        SELECT DATE(performed_at) AS day, COUNT(*) AS cnt
        FROM activity_logs
        WHERE performed_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)
        GROUP BY day ORDER BY day
        """
    )
    activity_7d = [
        {"date": str(row["day"]), "count": int(row["cnt"])}
        for _, row in activity_df.iterrows()
    ]

    # ── Cảnh báo đi muộn tháng này ──────────────────────────────────
    late_df = query_df(
        """
        SELECT e.employee_code, e.full_name, COUNT(*) AS late_count
        FROM attendances a
        JOIN employees e ON a.employee_id = e.id
        WHERE a.status = 'LATE'
          AND MONTH(a.attendance_date) = :month AND YEAR(a.attendance_date) = :year
        GROUP BY e.id ORDER BY late_count DESC LIMIT 5
        """,
        {"month": month, "year": year},
    )
    top_late = late_df.to_dict(orient="records")

    return {
        "month": month,
        "year": year,
        "employees": {
            "active": total_active,
            "byStatus": emp_stats,
        },
        "attendance": {
            "rate": attendance_rate,
            "byStatus": att_stats,
        },
        "pendingLeaveRequests": int(pending_leaves),
        "payroll": payroll_summary,
        "activityLast7Days": activity_7d,
        "topLateEmployees": top_late,
    }
