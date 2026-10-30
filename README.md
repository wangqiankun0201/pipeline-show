# 管道完整性展示平台（PIMS，Pipeline Integrity Management System）

面向西南油气田梦想云平台的管道展示与分析应用，包含三个业务页面：

| 页面 | 路由 | 后端服务 | 说明 |
|---|---|---|---|
| 管道位置 | `/gis` | `pims-gis` | 管段 GIS 数据映射到底图，管线高亮（光晕 + 流动线），点击查看位置参数 |
| 内腐蚀趋势预测 | `/corrosion` | `pims-corrosion` | 指定三管段非线性幂律 `d(t)=ktⁿ`：实测对比、投运至2031年趋势（达壁厚截止） |
| 失效风险计算 | `/risk` | `pims-risk` | 最新内检条件下的非齐次 Gamma 腐蚀过程，解析累计穿透概率与蒙特卡洛校核 |

每个页面都有独立 URL，并提供无框架的 `/widget/{gis|corrosion|risk}` 版本，可作为梦想云微件嵌入。

## 技术栈（遵循《西南油气田梦想云环境云原生开发实施指导方案》）

- **前端**：Vue 3 + Ant-Design-Vue 4 + TypeScript + Vite，OpenLayers（地图）、ECharts（曲线）、Pinia、Vue Router
- **后端**：Spring Boot 2.6.8、Spring Cloud 2021.0.1、Spring Cloud Alibaba 2021.0.1.0
  - Nacos（注册/配置中心）、Spring Cloud Gateway、OpenFeign + LoadBalancer + Sentinel
  - MyBatis-Plus 3.5.2、Druid 1.2.11、PostgreSQL、Swagger（springfox 3.0.0）、JDK 1.8 编译目标
- **鉴权**：网关调用梦想云用户中心 `/api/v2/sys/user/currentuser` 校验 Bearer 令牌
- **部署**：Dockerfile + 梦想云 PaaS 流水线，健康检查 `/actuator/health`

## 目录结构

```
pipeline-platform/
├── backend/                     后端（Maven 多模块）
│   ├── pom.xml                  父工程，统一组件版本
│   ├── ruidao-settings.xml      流水线 Maven 配置
│   ├── pims-common/             统一返回、异常处理、用户上下文、工具类、Swagger
│   ├── pims-gateway/   :9100    网关：路由、令牌校验、仅开放 GET/HEAD/POST
│   ├── pims-gis/       :9101    管段基础数据与 GIS 空间数据、GeoJSON 导入
│   ├── pims-corrosion/ :9102    内检数据导入、幂律参数库、趋势预测、计算留痕
│   └── pims-risk/      :9103    条件Gamma穿透概率、路径模拟及旧版评价接口
├── frontend/pims-web/           前端（Vue3 + AntDV + TS）
├── deploy/
│   ├── sql/                     PostgreSQL 初始化脚本（tools/build_sql.py 生成）
│   ├── nacos/                   各服务 Nacos 配置样例
│   ├── templates/               GIS 数据导入模板（GeoJSON）
│   └── local/                   本地一键启动脚本
├── docs/                        接口文档、设计说明、部署说明
└── tools/                       代码/脚本生成工具
```

后端分层与命名遵循规范：`XxxController` → `IXxxInterFace`（接口）/ `XxxImpl`（实现）→ `XxxDAO`（MyBatis-Plus Mapper）→ `XxxModel`（与表对应），常量和枚举命名为 `XxxType`，工具类命名为 `XxxUtil`。类和方法均使用规范规定的中文注释头。

## 本地运行

环境：JDK 8+（已在 JDK 21 运行验证）、Maven 3.6+、Node.js 18+。本地 `local` 配置使用 H2 内存库（PostgreSQL 兼容模式），不连接 Nacos，并关闭鉴权，不需要任何中间件。

```bash
cd backend && mvn clean package
```

```bash
bash deploy/local/start-backend.sh
```

```bash
cd frontend/pims-web && npm install && npm run dev
```

浏览器访问 http://localhost:5173 。各服务的 Swagger 文档地址为 `http://localhost:910x/swagger-ui/index.html`。

启动时 `pims-corrosion` 会自动导入内置的《内腐蚀数据.xlsx》，`pims-gis` 会写入 6 个管段的**示意坐标**。

## 当前数据与待办

1. **GIS 坐标为示意数据**：提供的资料中没有管道坐标，目前按万州地区的走向绘制了示意线（页面会标注“示意坐标”）。拿到正式 GIS 数据后，按 `deploy/templates/pipe-segment-gis-template.geojson` 的格式（WGS84，`properties.segmentId` 对应管段ID）在页面“导入GIS数据”即可替换。
2. **腐蚀穿透模型**：已接入最新内检条件下的 Gamma 增量模型，默认 θ=0.2 mm，提供解析上尾概率和固定种子蒙特卡洛校核。θ 尚未由现场样本标定，结果不等同综合事故概率。旧版因素评价接口保留兼容。
3. **内网底图**：`deploy/nacos/pims-gis.yaml` 已给出天地图配置（需申请 tk）。本地默认使用 OSM、CARTO 和 Esri 的互联网瓦片。
4. 腐蚀模型的使用边界见页面提示和《技术说明》：结果仅用于趋势情景推演，不能单独作为停输或维修决策依据。

详见 [docs/设计说明.md](docs/设计说明.md)、[docs/部署说明.md](docs/部署说明.md)、[docs/接口文档.md](docs/接口文档.md)。
