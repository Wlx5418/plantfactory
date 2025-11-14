#!/bin/bash

# 植物工厂管理系统构建脚本
# 用于构建整个项目

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 项目根目录
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

log_info "项目根目录: $PROJECT_ROOT"

# 检查必要工具
check_tools() {
    log_info "检查构建工具..."

    # 检查Node.js
    if ! command -v node &> /dev/null; then
        log_error "Node.js 未安装"
        exit 1
    fi
    log_info "Node.js 版本: $(node --version)"

    # 检查npm
    if ! command -v npm &> /dev/null; then
        log_error "npm 未安装"
        exit 1
    fi
    log_info "npm 版本: $(npm --version)"

    # 检查Java
    if ! command -v java &> /dev/null; then
        log_error "Java 未安装"
        exit 1
    fi
    log_info "Java 版本: $(java --version | head -n 1)"

    # 检查Maven
    if ! command -v mvn &> /dev/null; then
        log_error "Maven 未安装"
        exit 1
    fi
    log_info "Maven 版本: $(mvn --version | head -n 1)"
}

# 构建前端
build_frontend() {
    log_info "开始构建前端..."

    cd "$PROJECT_ROOT/plantfactoryvue"

    # 安装依赖
    log_info "安装前端依赖..."
    npm ci

    # 构建生产版本
    log_info "构建前端生产版本..."
    npm run build

    # 复制构建结果到项目根目录
    if [ -d "dist" ]; then
        cp -r dist/* "$PROJECT_ROOT/dist/"
        log_info "前端构建完成"
    else
        log_error "前端构建失败：dist 目录不存在"
        exit 1
    fi

    cd "$PROJECT_ROOT"
}

# 构建后端
build_backend() {
    log_info "开始构建后端..."

    cd "$PROJECT_ROOT/plantfactoryspring"

    # 清理并构建
    log_info "构建后端应用..."
    ./mvnw clean package -DskipTests

    # 检查构建结果
    if [ -f "target/plantfactory-*.jar" ]; then
        log_info "后端构建完成"
    else
        log_error "后端构建失败：jar 文件不存在"
        exit 1
    fi

    cd "$PROJECT_ROOT"
}

# 创建Docker镜像
build_docker() {
    log_info "构建Docker镜像..."

    # 构建应用镜像
    docker build -t plantfactory:latest .

    if [ $? -eq 0 ]; then
        log_info "Docker镜像构建完成"
    else
        log_error "Docker镜像构建失败"
        exit 1
    fi
}

# 清理构建文件
cleanup() {
    log_info "清理构建文件..."
    rm -rf "$PROJECT_ROOT/dist"
}

# 主函数
main() {
    log_info "开始构建植物工厂管理系统..."

    # 检查工具
    check_tools

    # 清理旧的构建文件
    cleanup

    # 创建构建目录
    mkdir -p "$PROJECT_ROOT/dist"

    # 构建前端
    build_frontend

    # 构建后端
    build_backend

    # 构建Docker镜像
    if command -v docker &> /dev/null; then
        build_docker
    else
        log_warn "Docker 未安装，跳过镜像构建"
    fi

    log_info "构建完成！"
    log_info "前端文件位置: $PROJECT_ROOT/dist"
    log_info "后端文件位置: $PROJECT_ROOT/plantfactoryspring/target/"
}

# 执行主函数
main "$@"