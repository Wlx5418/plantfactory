# 多阶段构建
# 第一阶段：构建应用
FROM maven:3.8.6-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制pom.xml
COPY plantfactoryspring/pom.xml .
COPY plantfactoryspring/.mvn .mvn
COPY plantfactoryspring/mvnw .

# 下载依赖（利用Docker缓存）
RUN ./mvnw dependency:go-offline

# 复制源代码
COPY plantfactoryspring/src ./src

# 构建应用
RUN ./mvnw clean package -DskipTests

# 第二阶段：运行应用
FROM openjdk:17-jre-slim

# 安装必要的工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r appuser && useradd -r -g appuser appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/target/plantfactory-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# 切换到非root用户
USER appuser

# 暴露端口
EXPOSE 8080

# 设置JVM参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport"

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]