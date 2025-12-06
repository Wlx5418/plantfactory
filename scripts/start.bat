@echo off
echo ===== 植物工厂管理系统 - 启动脚本 =====
echo.

:: 检查端口占用情况
echo 检查端口占用情况...
netstat -ano | findstr ":9091" | find "LISTENING" >nul
if %errorlevel% equ 0 (
    echo ⚠ 警告: 9091端口已被占用！
    echo 请先运行 kill_ports.bat 清理端口
    pause
    exit /b 1
)

netstat -ano | findstr ":5173" | find "LISTENING" >nul
if %errorlevel% equ 0 (
    echo ⚠ 警告: 5173端口已被占用！
    echo 请先运行 kill_ports.bat 清理端口
    pause
    exit /b 1
)

echo ✓ 端口检查通过
echo.

:: 启动后端服务
echo 步骤1: 启动后端服务 (端口9091)...
cd /d "%~dp0..\plantfactoryspring"
start "植物工厂-后端" cmd /k "echo 植物工厂管理系统 - 后端服务正在启动... && echo 服务地址: http://localhost:9091/api && echo. && mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=9091 -Dmaven.test.skip=true"

echo.
echo 等待后端服务启动 (20秒)...
timeout /t 20 /nobreak

echo.
echo 步骤2: 启动前端服务 (端口5173)...
cd /d "%~dp0..\plantfactoryvue"
start "植物工厂-前端" cmd /k "echo 植物工厂管理系统 - 前端服务正在启动... && echo 服务地址: http://localhost:5173 && echo. && npm run dev"

echo.
echo ===== 启动完成 =====
echo.
echo 访问地址：
echo 前端应用: http://localhost:5173
echo 后端API:  http://localhost:9091/api
echo.
echo 默认登录账号: admin / admin123456
echo.
echo 如需停止服务，请运行: scripts\kill_ports.bat
echo.
pause