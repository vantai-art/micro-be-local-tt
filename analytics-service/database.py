from sqlalchemy import create_engine, text
from sqlalchemy.orm import sessionmaker
import pandas as pd
from config import DATABASE_URL

engine = create_engine(DATABASE_URL, pool_pre_ping=True, pool_recycle=3600)
SessionLocal = sessionmaker(bind=engine)


def query_df(sql: str, params: dict = None) -> pd.DataFrame:
    """Chạy raw SQL và trả về DataFrame. Tương thích SQLAlchemy 2.0."""
    with engine.connect() as conn:
        stmt = text(sql)
        if params:
            # SQLAlchemy 2.0: dùng bindparams thay vì truyền dict trực tiếp
            stmt = stmt.bindparams(**{k: v for k, v in params.items() if v is not None})
        return pd.read_sql_query(stmt, conn)


def scalar(sql: str, params: dict = None):
    """Trả về giá trị đơn."""
    with engine.connect() as conn:
        stmt = text(sql)
        if params:
            stmt = stmt.bindparams(**{k: v for k, v in params.items() if v is not None})
        result = conn.execute(stmt)
        row = result.fetchone()
        return row[0] if row else None
