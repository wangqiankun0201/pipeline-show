<template>
  <div class="segment-panel">
    <div class="panel-header">
      <span class="panel-title"><ApartmentOutlined /> 管段目录</span>
      <span class="panel-count">共 {{ segments.length }} 段</span>
    </div>
    <div class="panel-search">
      <a-input v-model:value="keyword" allow-clear placeholder="搜索管线 / 管段 / 气矿" size="middle">
        <template #prefix><SearchOutlined class="text-secondary" /></template>
      </a-input>
    </div>
    <div class="panel-body">
      <a-spin :spinning="loading">
        <a-empty v-if="!loading && !groups.length" description="暂无管段数据" class="panel-empty" />
        <div v-for="group in groups" :key="group.pipelineName" class="pipe-group">
          <div class="group-title">
            <span class="group-swatch" :style="{ background: colors[group.pipelineName] || '#2470ff' }" />
            <span class="group-name">{{ group.pipelineName }}</span>
            <span class="group-mine">{{ group.mine }}</span>
          </div>
          <div
            v-for="item in group.items"
            :key="item.segmentId"
            class="segment-item"
            :class="{ active: item.segmentId === selectedId }"
            @click="emit('select', item.segmentId)"
          >
            <div class="item-main">
              <span class="item-name">{{ item.segmentName }}</span>
              <a-tag v-if="item.coordStatus === 'SCHEMATIC'" color="orange" class="item-tag">示意</a-tag>
            </div>
            <div class="item-sub">
              <span>{{ fmtNum(item.lengthKm, 1) }} km</span>
              <span>Φ{{ fmtText(item.outerDiameterMm) }}×{{ fmtText(item.wallThicknessMm) }}</span>
              <span>{{ fmtText(item.materialGrade) }}</span>
            </div>
          </div>
        </div>
      </a-spin>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { ApartmentOutlined, SearchOutlined } from '@ant-design/icons-vue'
import type { SegmentProperties } from '@/types/gis'
import { fmtNum, fmtText } from '@/utils/format'

const props = defineProps<{
  segments: SegmentProperties[]
  selectedId: number | null
  colors: Record<string, string>
  loading?: boolean
}>()
const emit = defineEmits<{ (e: 'select', segmentId: number): void }>()

const keyword = ref('')

const groups = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  const filtered = props.segments.filter(
    (s) => !kw || [s.segmentName, s.pipelineName, s.mine, s.workArea].some((v) => v?.toLowerCase().includes(kw))
  )
  const map = new Map<string, { pipelineName: string; mine: string; items: SegmentProperties[] }>()
  filtered.forEach((s) => {
    if (!map.has(s.pipelineName)) map.set(s.pipelineName, { pipelineName: s.pipelineName, mine: s.mine, items: [] })
    map.get(s.pipelineName)!.items.push(s)
  })
  return Array.from(map.values())
})
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.segment-panel {
  width: 300px;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  background: rgba(255, 255, 255, 0.96);
  border-radius: 2px;
  box-shadow: 0 4px 16px rgba(0, 32, 96, 0.16);
  overflow: hidden;
}

.panel-header {
  height: 48px;
  flex: none;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: @primary;
  color: #fff;
}

.panel-title {
  font-size: 16px;
  font-weight: 700;
  display: flex;
  gap: 8px;
  align-items: center;
}

.panel-count {
  font-size: 12px;
  opacity: 0.85;
}

.panel-search {
  padding: 12px;
  border-bottom: 1px solid @border;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 4px 0 12px;
  min-height: 120px;
}

.panel-empty {
  margin-top: 32px;
}

.pipe-group {
  margin-top: 8px;
}

.group-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  font-size: 14px;
  font-weight: 700;
  color: @text-main;
}

.group-swatch {
  width: 18px;
  height: 4px;
  border-radius: 2px;
  box-shadow: 0 0 6px currentColor;
}

.group-mine {
  margin-left: auto;
  font-size: 12px;
  font-weight: 400;
  color: @text-secondary;
}

.segment-item {
  margin: 2px 8px;
  padding: 8px 12px 8px 30px;
  border-radius: 2px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;

  &:hover {
    background: #f5f8ff;
  }

  &.active {
    background: #eff4ff;
    border-color: @primary;
  }
}

.item-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-name {
  font-size: 14px;
  color: @text-main;
}

.item-tag {
  margin-inline-end: 0;
  font-size: 12px;
  line-height: 18px;
}

.item-sub {
  margin-top: 2px;
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: @text-secondary;
}
</style>
