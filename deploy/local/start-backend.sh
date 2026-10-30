#!/usr/bin/env bash
# 本地一键启动后端（local 配置：H2 内存库、无 Nacos、关闭鉴权）
# 用法：先在 backend 目录执行 mvn clean package -DskipTests，再运行本脚本
set -e
ROOT="$(cd "$(dirname "$0")/../.." && pwd)"
LOG_DIR="$ROOT/logs"
mkdir -p "$LOG_DIR"
export PIMS_PROFILE=local

start() {
  local name="$1"
  nohup java -jar "$ROOT/backend/$name/target/$name.jar" > "$LOG_DIR/$name.log" 2>&1 &
  echo "已启动 $name（PID $!），日志：logs/$name.log"
}

start pims-gis
start pims-corrosion
start pims-risk
start pims-gateway
echo "网关地址：http://localhost:9100   各服务 Swagger：http://localhost:9101~9103/swagger-ui/index.html"
