<template>
  <div class="map-legend">
    <div class="legend-title">图例</div>
    <div v-for="(color, name) in colors" :key="name" class="legend-row">
      <span class="legend-line" :style="{ background: color, boxShadow: `0 0 6px ${color}` }" />
      <span>{{ name }}</span>
    </div>
    <div class="legend-row">
      <span class="legend-dot start" />
      <span>起点</span>
      <span class="legend-dot end" />
      <span>终点</span>
    </div>
    <div class="legend-row">
      <span class="legend-line selected" />
      <span>选中管段</span>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ colors: Record<string, string> }>()
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.map-legend {
  background: rgba(255, 255, 255, 0.96);
  padding: 10px 14px;
  border-radius: 2px;
  box-shadow: 0 2px 10px rgba(0, 32, 96, 0.16);
  font-size: 12px;
  color: @text;
  min-width: 150px;
}

.legend-title {
  font-size: 14px;
  font-weight: 700;
  color: @text-main;
  margin-bottom: 6px;
}

.legend-row {
  display: flex;
  align-items: center;
  gap: 8px;
  line-height: 24px;
}

.legend-line {
  width: 28px;
  height: 4px;
  border-radius: 2px;

  &.selected {
    background: #2470ff;
    box-shadow: 0 0 0 4px rgba(11, 42, 107, 0.28);
  }
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;

  &.start {
    background: @primary;
    border: 2px solid #fff;
    box-shadow: 0 0 0 1px @primary;
  }

  &.end {
    background: #fff;
    border: 2px solid @primary;
  }
}
</style>
