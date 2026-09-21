@echo off
cd /d "%~dp0"
python -m http.server 8000
echo 服务器已启动，请在浏览器中访问 http://localhost:8000
pause