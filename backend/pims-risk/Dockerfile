# PIMS 失效风险计算服务镜像（流水线名称/镜像名称：pims-risk-java，版本号 ${GIT_BRANCH_AS_TAG}_${GIT_COMMIT}）
# 基础镜像可替换为梦想云平台提供的 JDK8 基础镜像
ARG BASE_IMAGE=eclipse-temurin:8-jre
FROM ${BASE_IMAGE}
ENV TZ=Asia/Shanghai \
    PIMS_PROFILE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"
WORKDIR /app
COPY target/pims-risk.jar /app/app.jar
EXPOSE 9103
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
