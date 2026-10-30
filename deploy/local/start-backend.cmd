@echo off
rem 本地一键启动后端（local 配置：H2 内存库、无 Nacos、关闭鉴权）
rem 用法：先在 backend 目录执行 mvn clean package -DskipTests，再运行本脚本
chcp 65001 >nul
set ROOT=%~dp0..\..
set PIMS_PROFILE=local
if not exist "%ROOT%\logs" mkdir "%ROOT%\logs"
for %%S in (pims-gis pims-corrosion pims-risk pims-gateway) do (
  start "%%S" /min cmd /c "java -jar "%ROOT%\backend\%%S\target\%%S.jar" > "%ROOT%\logs\%%S.log" 2>&1"
  echo 已启动 %%S，日志：logs\%%S.log
)
echo 网关地址：http://localhost:9100
