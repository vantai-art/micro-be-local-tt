from fastapi import APIRouter, Query
from database import query_df
from datetime import date, timedelta
from typing import Optional

router = APIRouter(prefix="/analytics/activity-logs", tags=["Activity Log Analytics"])


@router.get("/by-action")
def by_action(
    from_date: str = Query(default=str(date.today() - timedelta(days=30))),
    to_date: str = Query(default=str(date.today())),
):
    """
    Số lần thực hiện mỗi loại hành động trong khoảng thời gian.
    Dùng cho biểu đồ tròn hoặc cột.
    """
    df = query_df(
        """
        SELECT
            action,
            COUNT(*) AS count
        FROM activity_logs
        WHERE DATE(performed_at) BETWEEN :from_date AND :to_date
        GROUP BY action
        ORDER BY count DESC
        """,
        {"from_date": from_date, "to_date": to_date},
    )
    return df.to_dict(orient="records")


@router.get("/by-user")
def by_user(
    from_date: str = Query(default=str(date.today() - timedelta(days=30))),
    to_date: str = Query(default=str(date.today())),
):
    """
    Số hành động mỗi người thực hiện — ai làm nhiều nhất.
    Dùng cho biểu đồ cột.
    """
    df = query_df(
        """
        SELECT
            performed_by,
            COUNT(*) AS total_actions,
            COUNT(DISTINCT action) AS distinct_actions,
            MIN(performed_at)      AS first_action,
            MAX(performed_at)      AS last_action
        FROM activity_logs
        WHERE DATE(performed_at) BETWEEN :from_date AND :to_date
        GROUP BY performed_by
        ORDER BY total_actions DESC
        """,
        {"from_date": from_date, "to_date": to_date},
    )
    return df.to_dict(orient="records")


@router.get("/timeline")
def timeline(
    target_date: str = Query(default=str(date.today())),
    performed_by: Optional[str] = None,
):
    """
    Timeline hoạt động trong một ngày theo giờ.
    Dùng cho biểu đồ đường (trục X = giờ, trục Y = số hành động).
    """
    user_filter = "AND performed_by = :user" if performed_by else ""
    df = query_df(
        f"""
        SELECT
            HOUR(performed_at) AS hour,
            COUNT(*)           AS count,
            GROUP_CONCAT(DISTINCT action ORDER BY action SEPARATOR ', ') AS actions
        FROM activity_logs
        WHERE DATE(performed_at) = :target_date
          {user_filter}
        GROUP BY hour
        ORDER BY hour
        """,
        {"target_date": target_date, "user": performed_by},
    )
    return df.to_dict(orient="records")


@router.get("/outside-hours")
def outside_hours(
    from_date: str = Query(default=str(date.today() - timedelta(days=7))),
    to_date: str = Query(default=str(date.today())),
    start_hour: int = Query(default=7, description="Giờ bắt đầu ca (mặc định 7:00)"),
    end_hour: int = Query(default=19, description="Giờ kết thúc ca (mặc định 19:00)"),
):
    """
    Phát hiện hành động thực hiện ngoài giờ làm việc — cảnh báo bất thường.
    """
    df = query_df(
        """
        SELECT
            performed_by,
            action,
            target_type,
            target_name,
            performed_at,
            summary
        FROM activity_logs
        WHERE DATE(performed_at) BETWEEN :from_date AND :to_date
          AND (HOUR(performed_at) < :start_h OR HOUR(performed_at) >= :end_h)
        ORDER BY performed_at DESC
        """,
        {
            "from_date": from_date,
            "to_date": to_date,
            "start_h": start_hour,
            "end_h": end_hour,
        },
    )
    # Chuyển datetime sang string để JSON serializable
    df["performed_at"] = df["performed_at"].astype(str)
    return df.to_dict(orient="records")


@router.get("/summary-stats")
def summary_stats(
    from_date: str = Query(default=str(date.today() - timedelta(days=30))),
    to_date: str = Query(default=str(date.today())),
):
    """
    Thống kê tổng hợp activity log: tổng hành động, người dùng hoạt động, hành động phổ biến nhất.
    """
    df = query_df(
        """
        SELECT
            COUNT(*)                    AS total_actions,
            COUNT(DISTINCT performed_by) AS active_users,
            COUNT(DISTINCT DATE(performed_at)) AS active_days,
            SUM(action LIKE 'DELETE%')  AS delete_count,
            SUM(action LIKE 'UPDATE%')  AS update_count,
            SUM(action LIKE 'CREATE%')  AS create_count
        FROM activity_logs
        WHERE DATE(performed_at) BETWEEN :from_date AND :to_date
        """,
        {"from_date": from_date, "to_date": to_date},
    )
    row = df.iloc[0].to_dict() if not df.empty else {}
    return {k: (int(v) if v is not None else 0) for k, v in row.items()}
