@echo off
REM 植物工厂管理系统构建脚本 (Windows版本)

setlocal enabledelayedexpansion

echo [INFO] 植物工厂管理系统构建脚本
echo [INFO] 项目根目录: %~dp0..

REM 设置项目根目录
set PROJECT_ROOT=%~dp0..
cd /d "%PROJECT_ROOT%"

echo [INFO] 检查构建工具...

REM 检查Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Node.js 未安装
    pause
    exit /b 1
)
echo [INFO] Node.js 版本:
node --version

REM 检查npm
npm --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] npm 未安装
    pause
    exit /b 1
)
echo [INFO] npm 版本:
npm --version

REM 检查Java
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Java 未安装
    pause
    exit /b 1
)

REM 检查Maven
mvn --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Maven 未安装
    pause
    exit /b 1
)

echo [INFO] 开始构建前端...
cd /d "%PROJECT_ROOT%\plantfactoryvue"

REM 安装前端依赖
echo [INFO] 安装前端依赖...
npm ci
if %errorlevel% neq 0 (
    echo [ERROR] 前端依赖安装失败
    pause
    exit /b 1
)

REM 构建前端生产版本
echo [INFO] 构建前端生产版本...
npm run build
if %errorlevel% neq 0 (
    echo [ERROR] 前端构建失败
    pause
    exit /b 1
)

REM 检查构建结果
if not exist "dist" (
    echo [ERROR] 前端构建失败：dist 目录不存在
    pause
    exit /b 1
)

REM 复制构建结果到项目根目录
echo [INFO] 复制前端文件...
if not exist "%PROJECT_ROOT%\dist" mkdir "%PROJECT_ROOT%\dist"
xcopy "dist\*" "%PROJECT_ROOT%\dist\" /E /Y
if %errorlevel% neq 0 (
    echo [ERROR] 复制前端文件失败
    pause
    exit /b 1
)

echo [INFO] 前端构建完成

cd /d "%PROJECT_ROOT%"

echo [INFO] 开始构建后端...
cd /d "%PROJECT_ROOT%\plantfactoryspring"

REM 清理并构建后端
echo [INFO] 构建后端应用...
call mvnw.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo [ERROR] 后端构建失败
    pause
    exit /b 1
)

REM 检查构建结果
if not exist "target\plantfactory-*.jar" (
    echo [ERROR] 后端构建失败：jar 文件不存在
    pause
    exit /b 1
)

echo [INFO] 后端构建完成

cd /d "%PROJECT_ROOT%"

REM 检查Docker
docker --version >nul 2>&1
if %errorlevel% equ 0 (
    echo [INFO] 构建Docker镜像...
    docker build -t plantfactory:latest .
    if %errorlevel% equ 0 (
        echo [INFO] Docker镜像构建完成
    ) else (
        echo [ERROR] Docker镜像构建失败
    )
) else (
    echo [WARN] Docker 未安装，跳过镜像构建
)

echo.
echo [INFO] 构建完成！
echo [INFO] 前端文件位置: %PROJECT_ROOT%\dist
echo [INFO] 后端文件位置: %PROJECT_ROOT%\plantfactoryspring\target\
echo.
pause