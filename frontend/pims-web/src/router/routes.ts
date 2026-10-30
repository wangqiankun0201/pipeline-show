import type { RouteRecordRaw } from 'vue-router'
import BasicLayout from '@/layouts/BasicLayout.vue'

export interface MenuMeta {
  title: string
  icon: string
  /** 页面铺满内容区（地图页无内边距） */
  fullBleed?: boolean
}

/** 菜单路由：每个页面具备独立 URL，可单独发布为梦想云微件 */
export const menuRoutes: RouteRecordRaw[] = [
  {
    path: 'gis',
    name: 'Gis',
    component: () => import('@/views/gis/GisMapView.vue'),
    meta: { title: '管道位置', icon: 'EnvironmentOutlined', fullBleed: true } satisfies MenuMeta
  },
  {
    path: 'corrosion',
    name: 'Corrosion',
    component: () => import('@/views/corrosion/CorrosionView.vue'),
    meta: { title: '内腐蚀趋势预测', icon: 'LineChartOutlined' } satisfies MenuMeta
  },
  {
    path: 'risk',
    name: 'Risk',
    component: () => import('@/views/risk/RiskView.vue'),
    meta: { title: '失效风险计算', icon: 'AlertOutlined' } satisfies MenuMeta
  }
]

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: BasicLayout,
    redirect: '/gis',
    children: menuRoutes
  },
  {
    // 微件模式：不带门户框架，仅渲染页面主体（供统一门户 iframe 嵌入）
    path: '/widget',
    component: () => import('@/layouts/WidgetLayout.vue'),
    children: menuRoutes.map((r) => ({ ...r, name: `Widget${String(r.name)}` }) as RouteRecordRaw)
  },
  { path: '/:pathMatch(.*)*', redirect: '/gis' }
]
