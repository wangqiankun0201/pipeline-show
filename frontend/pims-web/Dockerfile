# 管道完整性管理平台前端镜像（流水线名称/镜像名称：pims-portal-web）
# 多阶段构建：构建阶段使用 Node 18（Vite 5 要求 Node >= 18），与流水线自带 Node 版本解耦
FROM node:18-alpine AS build
WORKDIR /app
COPY package.json package-lock.json .npmrc ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:1.25-alpine
# 默认配置；梦想云上线时以配置字典 “pims-portal-web-niginx-configmap”（键 nginx.conf）挂载覆盖
COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
