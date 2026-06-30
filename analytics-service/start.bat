@echo off
echo Starting Analytics Service on port 8820...
cd /d %~dp0
pip install -r requirements.txt -q
python main.py
pause
