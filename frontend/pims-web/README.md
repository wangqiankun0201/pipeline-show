# pims-portal-web 管道完整性管理平台前端

技术栈（遵循《梦想云环境云原生开发实施指导方案》）：Vue 3 + TypeScript + Ant Design Vue 4，Vite 5 构建，
Vue Router / Pinia / Axios，ECharts 5（图表），OpenLayers 9（地图）。

## 目录结构

```
src/
├─ api/             接口层（http.ts 统一封装；gis / corrosion / risk / auth 按微服务划分）
├─ types/           与后端 VO 对应的 TS 类型
├─ layouts/         BasicLayout（60px 顶栏 + 260px 菜单）、WidgetLayout（微件模式，无框架）
├─ router/          路由与鉴权守卫（?token= 写入 sessionStorage）
├─ stores/          Pinia：当前用户、当前选中管段
├─ components/      通用组件：PageCard、StatCard、LimitFlagTag、GradeTag
├─ styles/          梦想云 UI 规范主题（theme.ts / variables.less / global.less）
├─ utils/           格式化、鉴权、表格、ECharts 按需注册
└─ views/
   ├─ gis/          管道位置（OpenLayers 地图，composables/usePipelineMap.ts 封装地图逻辑）
   ├─ corrosion/    内腐蚀趋势预测（参数卡、预测条件、结果、趋势曲线、数据表）
   └─ risk/         失效风险计算（因素表单、5×5 风险矩阵、结果、评价记录；公式待接入）
```

## 页面与微件地址

| 页面 | 门户内地址 | 微件（iframe）地址 |
|---|---|---|
| 管道位置 | `/gis` | `/widget/gis` |
| 内腐蚀趋势预测 | `/corrosion?segmentId=5` | `/widget/corrosion?segmentId=5` |
| 失效风险计算 | `/risk?segmentId=5` | `/widget/risk?segmentId=5` |

门户以 `?token=<梦想云令牌>` 方式进入，前端保存后在请求头携带 `Authorization: Bearer <token>`；
网关返回 401 / `code=unauthorized` 时清除令牌并跳转 `VITE_PORTAL_LOGIN_URL`。

## 本地开发

```bash
npm install
npm run dev
```

访问 http://localhost:5173 ，`/api` 代理到网关 `http://localhost:9100`（见 `.env.development`）。

## 构建与检查

```bash
npm run build   # vue-tsc 类型检查 + 生产构建，输出 dist/
npm run lint
```

## 梦想云发布

- 流水线名称 / 镜像名称：`pims-portal-web`，镜像版本 `${GIT_BRANCH_AS_TAG}_${GIT_COMMIT}`，master 分支，开启 SonarQube。
- 构建命令：`npm install; npm run build;`（项目已带 `.npmrc`，内网发布时改为内网源）。
  Vite 5 需要 Node ≥ 18；若流水线 Node 模板版本较低，使用本目录 `Dockerfile` 多阶段构建（构建阶段自带 Node 18）。
- Nginx：创建配置字典 `pims-portal-web-niginx-configmap`，键 `nginx.conf`，值为本目录 `nginx.conf`
  （按实际网关地址修改 `upstream pims_gateway`），通过存储卷挂载到 `/etc/nginx/nginx.conf`。
- 对外使用域名访问（如 `pims.dev.xnyqt.petrochina`），HTTP 80 / HTTPS 443。

## 地图底图

底图地址由后端 `GET /api/gis/map/config` 下发，前端不写死；内网上线后在后端配置切换为天地图 / 区域湖瓦片服务即可。
管段坐标为 WGS84（EPSG:4326），地图显示为 Web 墨卡托（EPSG:3857）。
