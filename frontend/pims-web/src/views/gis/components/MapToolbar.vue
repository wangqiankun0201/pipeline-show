<template>
  <div class="map-toolbar">
    <div class="basemap-switch">
      <div
        v-for="bm in basemaps"
        :key="bm.key"
        class="basemap-item"
        :class="{ active: bm.key === active }"
        @click="emit('switch-basemap', bm.key)"
      >
        <div class="basemap-thumb" :class="`thumb-${bm.key}`" />
        <span>{{ bm.name }}</span>
      </div>
    </div>
    <div class="tool-group">
      <a-tooltip title="放大" placement="left">
        <div class="tool-btn" @click="emit('zoom', 1)"><PlusOutlined /></div>
      </a-tooltip>
      <a-tooltip title="缩小" placement="left">
        <div class="tool-btn" @click="emit('zoom', -1)"><MinusOutlined /></div>
      </a-tooltip>
      <a-tooltip title="全部管线" placement="left">
        <div class="tool-btn" @click="emit('fit')"><ExpandOutlined /></div>
      </a-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ExpandOutlined, MinusOutlined, PlusOutlined } from '@ant-design/icons-vue'
import type { BasemapConfig } from '@/types/gis'

defineProps<{ basemaps: BasemapConfig[]; active: string }>()
const emit = defineEmits<{
  (e: 'switch-basemap', key: string): void
  (e: 'zoom', delta: number): void
  (e: 'fit'): void
}>()
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.map-toolbar {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.basemap-switch {
  display: flex;
  gap: 8px;
  padding: 6px;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 2px;
  box-shadow: 0 2px 10px rgba(0, 32, 96, 0.16);
}

.basemap-item {
  width: 72px;
  cursor: pointer;
  text-align: center;
  font-size: 12px;
  color: @text;
  border: 2px solid transparent;
  border-radius: 2px;
  padding-bottom: 2px;

  &.active {
    border-color: @primary;
    color: @primary;
  }
}

.basemap-thumb {
  height: 44px;
  margin-bottom: 2px;
  background: linear-gradient(135deg, #e8eef5 0%, #d3e3f1 50%, #f2efe6 100%);

  &.thumb-light {
    background: linear-gradient(135deg, #fafafa 0%, #eef1f4 50%, #e3e8ee 100%);
  }

  &.thumb-dark {
    background: linear-gradient(135deg, #1b2330 0%, #2c3646 50%, #3b4658 100%);
  }

  &.thumb-image {
    background: linear-gradient(135deg, #2f4a2a 0%, #5b6e3d 45%, #8b7d5a 100%);
  }
}

.tool-group {
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 2px;
  box-shadow: 0 2px 10px rgba(0, 32, 96, 0.16);
}

.tool-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: @text;
  font-size: 16px;

  & + & {
    border-top: 1px solid @border;
  }

  &:hover {
    color: @primary;
    background: #f5f8ff;
  }
}
</style>
