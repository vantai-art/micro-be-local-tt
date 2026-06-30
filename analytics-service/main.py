from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from routers import dashboard, attendance, activity_log, payroll, employees
from config import APP_PORT

app = FastAPI(
    title="HRM Analytics Service",
    description="Phân tích dữ liệu nhân sự: chấm công, lương, activity log, nhân viên",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
    allow_credentials=False,
)

app.include_router(dashboard.router)
app.include_router(attendance.router)
app.include_router(activity_log.router)
app.include_router(payroll.router)
app.include_router(employees.router)


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    return JSONResponse(
        status_code=500,
        content={"detail": str(exc), "type": type(exc).__name__},
        headers={"Access-Control-Allow-Origin": "*"},
    )


@app.get("/health")
def health():
    return {"status": "UP", "service": "analytics-service"}


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=APP_PORT, reload=True)
