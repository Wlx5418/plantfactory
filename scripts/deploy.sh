#!/bin/bash

# 植物工厂管理系统部署脚本
# 用于部署到生产环境

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

# 部署环境
ENVIRONMENT=${1:-production}

log_info "项目根目录: $PROJECT_ROOT"
log_info "部署环境: $ENVIRONMENT"

# 检查Docker和Docker Compose
check_docker() {
    log_info "检查Docker环境..."

    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装"
        exit 1
    fi
    log_info "Docker 版本: $(docker --version)"

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装"
        exit 1
    fi
    log_info "Docker Compose 版本: $(docker-compose --version)"
}

# 备份数据
backup_data() {
    log_info "备份数据库数据..."

    # 创建备份目录
    BACKUP_DIR="$PROJECT_ROOT/backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$BACKUP_DIR"

    # 备份MySQL数据
    if docker ps | grep -q plantfactory-mysql; then
        log_info "备份MySQL数据..."
        docker exec plantfactory-mysql mysqldump -u root -proot123456 --all-databases > "$BACKUP_DIR/mysql_backup.sql"
        log_info "MySQL数据备份完成: $BACKUP_DIR/mysql_backup.sql"
    fi

    # 备份Redis数据
    if docker ps | grep -q plantfactory-redis; then
        log_info "备份Redis数据..."
        docker exec plantfactory-redis redis-cli BGSAVE
        sleep 5
        docker cp plantfactory-redis:/data/dump.rdb "$BACKUP_DIR/redis_backup.rdb"
        log_info "Redis数据备份完成: $BACKUP_DIR/redis_backup.rdb"
    fi
}

# 停止现有服务
stop_services() {
    log_info "停止现有服务..."
    cd "$PROJECT_ROOT"
    docker-compose down
}

# 清理旧的镜像和容器
cleanup_docker() {
    log_info "清理Docker资源..."

    # 删除未使用的镜像
    docker image prune -f

    # 删除未使用的容器
    docker container prune -f

    # 删除未使用的卷
    docker volume prune -f
}

# 拉取最新代码
pull_code() {
    log_info "拉取最新代码..."
    cd "$PROJECT_ROOT"
    git pull origin main
}

# 构建项目
build_project() {
    log_info "构建项目..."
    cd "$PROJECT_ROOT"
    ./scripts/build.sh
}

# 启动服务
start_services() {
    log_info "启动服务..."
    cd "$PROJECT_ROOT"

    # 根据环境选择配置文件
    if [ "$ENVIRONMENT" = "production" ]; then
        docker-compose -f docker-compose.yml -f docker-compose.prod.yml up -d
    else
        docker-compose up -d
    fi

    # 等待服务启动
    log_info "等待服务启动..."
    sleep 30

    # 检查服务状态
    check_services
}

# 检查服务状态
check_services() {
    log_info "检查服务状态..."

    # 检查容器状态
    cd "$PROJECT_ROOT"
    docker-compose ps

    # 检查服务健康状态
    log_info "检查服务健康状态..."

    # 检查后端服务
    if curl -f http://localhost:8080/api/actuator/health &> /dev/null; then
        log_info "后端服务运行正常"
    else
        log_error "后端服务异常"
    fi

    # 检查前端服务
    if curl -f http://localhost/health &> /dev/null; then
        log_info "前端服务运行正常"
    else
        log_error "前端服务异常"
    fi

    # 检查数据库连接
    if docker exec plantfactory-mysql mysqladmin ping -h localhost &> /dev/null; then
        log_info "数据库连接正常"
    else
        log_error "数据库连接异常"
    fi

    # 检查Redis连接
    if docker exec plantfactory-redis redis-cli ping &> /dev/null; then
        log_info "Redis连接正常"
    else
        log_error "Redis连接异常"
    fi
}

# 初始化数据
init_data() {
    log_info "初始化数据..."

    # 检查是否需要初始化管理员用户
    ADMIN_EXISTS=$(docker exec plantfactory-mysql mysql -u root -proot123456 -D plant_factory -e "SELECT COUNT(*) FROM users WHERE username = 'admin';" 2>/dev/null | tail -1)

    if [ "$ADMIN_EXISTS" -eq 0 ]; then
        log_info "创建默认管理员用户..."
        docker exec plantfactory-mysql mysql -u root -proot123456 -D plant_factory -e "
            INSERT INTO users (username, password, email, real_name, status, created_at, updated_at)
            VALUES ('admin', '\$2a\$10\$YourHashedPasswordHere', 'admin@plantfactory.com', '系统管理员', 'ACTIVE', NOW(), NOW());
        "
        log_info "默认管理员用户已创建"
    else
        log_info "管理员用户已存在"
    fi
}

# 设置SSL证书
setup_ssl() {
    log_info "设置SSL证书..."

    SSL_DIR="$PROJECT_ROOT/ssl"
    mkdir -p "$SSL_DIR"

    # 如果没有证书文件，生成自签名证书
    if [ ! -f "$SSL_DIR/cert.pem" ] || [ ! -f "$SSL_DIR/key.pem" ]; then
        log_warn "未找到SSL证书，生成自签名证书..."

        openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
            -keyout "$SSL_DIR/key.pem" \
            -out "$SSL_DIR/cert.pem" \
            -subj "/C=CN/ST=Beijing/L=Beijing/O=Plant Factory/OU=IT/CN=localhost"

        log_info "自签名证书已生成: $SSL_DIR/"
        log_warn "生产环境请使用有效的SSL证书"
    else
        log_info "SSL证书已存在"
    fi
}

# 显示部署信息
show_deployment_info() {
    log_info "部署完成！"
    echo ""
    echo "=================================="
    echo "访问地址:"
    echo "前端: http://localhost"
    echo "HTTPS: https://localhost"
    echo "后端API: http://localhost/api"
    echo ""
    echo "默认管理员账号:"
    echo "用户名: admin"
    echo "密码: 123456"
    echo ""
    echo "服务状态:"
    docker-compose ps
    echo "=================================="
}

# 主函数
main() {
    log_info "开始部署植物工厂管理系统..."

    # 检查环境
    check_docker

    # 备份数据
    backup_data

    # 停止现有服务
    stop_services

    # 清理Docker资源
    cleanup_docker

    # 拉取最新代码
    pull_code

    # 构建项目
    build_project

    # 设置SSL证书
    setup_ssl

    # 启动服务
    start_services

    # 初始化数据
    init_data

    # 检查服务状态
    check_services

    # 显示部署信息
    show_deployment_info
}

# 执行主函数
main "$@"