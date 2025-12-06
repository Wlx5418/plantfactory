@echo off
echo ===== 植物工厂管理系统 - 端口清理脚本 =====
echo.

echo 检查并清理端口占用...

:: 清理9091端口
echo 检查9091端口...
netstat -ano | findstr ":9091" | find "LISTENING" >nul
if %errorlevel% equ 0 (
    echo 发现9091端口被占用，正在清理...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":9091" ^| find "LISTENING"') do (
        echo 终止PID: %%a
        taskkill /PID %%a /F >nul 2>&1
    )
)

:: 清理5173端口
echo 检查5173端口...
netstat -ano | findstr ":5173" | find "LISTENING" >nul
if %errorlevel% equ 0 (
    echo 发现5173端口被占用，正在清理...
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| find "LISTENING"') do (
        echo 终止PID: %%a
        taskkill /PID %%a /F >nul 2>&1
    )
)

:: 清理Java进程
echo 清理Java进程...
tasklist /FI "IMAGENAME eq java.exe" 2>nul | find "java.exe" >nul
if %errorlevel% equ 0 (
    echo 终止Java进程...
    taskkill /IM "java.exe" /F >nul 2>&1
)

:: 清理Node.js进程
echo 清理Node.js进程...
tasklist /FI "IMAGENAME eq node.exe" 2>nul | find "node.exe" >nul
if %errorlevel% equ 0 (
    echo 终止Node.js进程...
    taskkill /IM "node.exe" /F >nul 2>&1
)

echo.
echo 等待3秒确保端口完全释放...
timeout /t 3 /nobreak >nul

echo.
echo ===== 端口清理完成 =====
pause