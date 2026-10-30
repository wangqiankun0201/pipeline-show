<template>
  <div class="basic-layout">
    <header class="layout-header">
      <div class="brand">
        <img class="brand-logo" src="/favicon.svg" alt="logo" />
        <span class="brand-title">管道完整性管理平台</span>
      </div>
      <div class="header-right">
        <span class="header-clock num-font">{{ now }}</span>
        <a-divider type="vertical" class="header-divider" />
        <UserOutlined />
        <span class="user-name">{{ userStore.displayName }}</span>
      </div>
    </header>
    <div class="layout-body">
      <aside class="layout-sider">
        <a-menu mode="inline" :selected-keys="selectedKeys" class="side-menu" @click="onMenuClick">
          <a-menu-item v-for="item in menus" :key="item.path">
            <template #icon>
              <component :is="iconMap[item.icon]" />
            </template>
            {{ item.title }}
          </a-menu-item>
        </a-menu>
        <div class="sider-footer">梦想云 · 西南油气田</div>
      </aside>
      <main class="layout-content" :class="{ 'full-bleed': isFullBleed }">
        <router-view v-slot="{ Component: view }">
          <keep-alive :include="['GisMapView']">
            <component :is="view" />
          </keep-alive>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, type Component } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { AlertOutlined, EnvironmentOutlined, LineChartOutlined, UserOutlined } from '@ant-design/icons-vue'
import dayjs from 'dayjs'
import { menuRoutes, type MenuMeta } from '@/router/routes'
import { useUserStore } from '@/stores/user'

const iconMap: Record<string, Component> = { EnvironmentOutlined, LineChartOutlined, AlertOutlined }

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
userStore.loadCurrentUser()

const menus = menuRoutes.map((r) => ({ path: `/${r.path}`, ...(r.meta as unknown as MenuMeta) }))
const selectedKeys = computed(() => [`/${route.path.split('/')[1] || 'gis'}`])
const isFullBleed = computed(() => Boolean((route.meta as Partial<MenuMeta>)?.fullBleed))

function onMenuClick({ key }: { key: string | number }) {
  router.push(String(key))
}

const now = ref(dayjs().format('YYYY-MM-DD HH:mm:ss'))
let timer: number | undefined
onMounted(() => {
  timer = window.setInterval(() => (now.value = dayjs().format('YYYY-MM-DD HH:mm:ss')), 1000)
})
onBeforeUnmount(() => window.clearInterval(timer))
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.basic-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  min-width: 1280px;
}

.layout-header {
  height: @header-height;
  flex: none;
  background: @primary;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(23, 96, 232, 0.25);
  z-index: 10;
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-logo {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  border-radius: 4px;
}

.brand-title {
  font-size: 22px;
  font-weight: 700;
  letter-spacing: 1px;
}

.brand-sub {
  font-size: 12px;
  opacity: 0.7;
  margin-top: 6px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.header-clock {
  font-size: 16px;
  opacity: 0.9;
}

.header-divider {
  background: rgba(255, 255, 255, 0.4);
  height: 18px;
}

.layout-body {
  flex: 1;
  display: flex;
  min-height: 0;
}

.layout-sider {
  width: @sider-width;
  flex: none;
  background: #fff;
  border-right: 1px solid @border;
  display: flex;
  flex-direction: column;
}

.side-menu {
  flex: 1;
  border-inline-end: none !important;
  padding-top: 12px;
  font-size: 16px;

  :deep(.ant-menu-item) {
    height: 48px;
    line-height: 48px;
    border-radius: 2px;
    margin: 4px 12px;
    width: calc(100% - 24px);
  }

  :deep(.ant-menu-item-selected) {
    background: @primary;
    color: #fff;
  }
}

.sider-footer {
  padding: 16px;
  font-size: 12px;
  color: @text-secondary;
  text-align: center;
  border-top: 1px solid @border;
}

.layout-content {
  flex: 1;
  min-width: 0;
  overflow: auto;
  background: @bg;

  &.full-bleed {
    overflow: hidden;
  }
}
</style>
