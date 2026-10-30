<template>
  <transition name="slide">
    <div v-if="segment" class="detail-panel">
      <div class="detail-header" :style="{ borderTopColor: color }">
        <div class="detail-title-wrap">
          <div class="detail-title">{{ segment.segmentName }}</div>
          <div class="detail-sub">{{ segment.mine }} · {{ segment.pipelineName }}</div>
        </div>
        <CloseOutlined class="detail-close" @click="emit('close')" />
      </div>

      <div class="detail-body">
        <a-alert
          v-if="segment.coordStatus === 'SCHEMATIC'"
          type="warning"
          show-icon
          class="coord-alert"
          message="示意坐标"
          description="当前位置为示意坐标，仅用于展示，待导入实测 GIS 数据后替换。"
        />

        <div class="section-title">位置参数</div>
        <div class="endpoint-card">
          <div class="endpoint">
            <span class="dot start" :style="{ background: color }" />
            <div>
              <div class="endpoint-name">起点：{{ fmtText(segment.startName) }}</div>
              <div class="endpoint-coord num-font">{{ fmtCoord(segment.startLon, segment.startLat) }}</div>
            </div>
          </div>
          <div class="endpoint-line" :style="{ background: color }" />
          <div class="endpoint">
            <span class="dot end" :style="{ borderColor: color }" />
            <div>
              <div class="endpoint-name">终点：{{ fmtText(segment.endName) }}</div>
              <div class="endpoint-coord num-font">{{ fmtCoord(segment.endLon, segment.endLat) }}</div>
            </div>
          </div>
        </div>

        <a-descriptions :column="2" size="small" bordered class="detail-desc">
          <a-descriptions-item label="管段长度">{{ fmtNum(segment.lengthKm, 2) }} km</a-descriptions-item>
          <a-descriptions-item label="坐标状态">
            <a-tag :color="segment.coordStatus === 'SCHEMATIC' ? 'orange' : 'green'">
              {{ segment.coordStatusName || (segment.coordStatus === 'SCHEMATIC' ? '示意坐标' : '实测坐标') }}
            </a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="气矿">{{ fmtText(segment.mine) }}</a-descriptions-item>
          <a-descriptions-item label="作业区">{{ fmtText(segment.workArea) }}</a-descriptions-item>
          <a-descriptions-item label="管线">{{ fmtText(segment.pipelineName) }}</a-descriptions-item>
          <a-descriptions-item label="管段">{{ fmtText(segment.segmentName) }}</a-descriptions-item>
          <a-descriptions-item label="投运日期">{{ fmtText(segment.commissionDate) }}</a-descriptions-item>
          <a-descriptions-item label="介质">{{ fmtText(segment.mediumType) }}</a-descriptions-item>
          <a-descriptions-item label="外径">{{ fmtText(segment.outerDiameterMm) }} mm</a-descriptions-item>
          <a-descriptions-item label="壁厚">{{ fmtText(segment.wallThicknessMm) }} mm</a-descriptions-item>
          <a-descriptions-item label="材质">{{ fmtText(segment.materialGrade) }}</a-descriptions-item>
          <a-descriptions-item label="设计压力">{{ fmtText(segment.designPressureMpa) }} MPa</a-descriptions-item>
          <a-descriptions-item label="数据来源" :span="2">{{ fmtText(segment.dataSource) }}</a-descriptions-item>
        </a-descriptions>
      </div>

      <div class="detail-footer">
        <a-button type="primary" @click="goCorrosion"><LineChartOutlined /> 内腐蚀趋势</a-button>
        <a-button @click="goRisk"><AlertOutlined /> 失效风险</a-button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { AlertOutlined, CloseOutlined, LineChartOutlined } from '@ant-design/icons-vue'
import type { SegmentProperties } from '@/types/gis'
import { fmtCoord, fmtNum, fmtText } from '@/utils/format'

const props = defineProps<{ segment: SegmentProperties | null; color: string }>()
const emit = defineEmits<{ (e: 'close'): void }>()
const router = useRouter()

function goCorrosion() {
  if (props.segment) router.push({ path: '/corrosion', query: { segmentId: props.segment.segmentId } })
}

function goRisk() {
  if (props.segment) router.push({ path: '/risk', query: { segmentId: props.segment.segmentId } })
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.detail-panel {
  width: 400px;
  max-height: 100%;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 2px;
  box-shadow: 0 4px 16px rgba(0, 32, 96, 0.18);
  overflow: hidden;
}

.detail-header {
  flex: none;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 14px 16px;
  border-top: 4px solid @primary;
  border-bottom: 1px solid @border;
  background: linear-gradient(90deg, #e5effe 0%, #f6faff 100%);
}

.detail-title {
  font-size: 18px;
  font-weight: 700;
  color: @text-main;
}

.detail-sub {
  font-size: 12px;
  color: @text-secondary;
  margin-top: 2px;
}

.detail-close {
  cursor: pointer;
  color: @text-secondary;
  font-size: 16px;
  padding: 4px;
  &:hover {
    color: @primary;
  }
}

.detail-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 16px;
}

.coord-alert {
  margin-bottom: 12px;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 8px;
  color: @text-main;
}

.endpoint-card {
  background: #f7fafb;
  border: 1px solid @border;
  padding: 12px;
  margin-bottom: 12px;
}

.endpoint {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.endpoint-name {
  font-size: 14px;
  color: @text-main;
}

.endpoint-coord {
  font-size: 13px;
  color: @primary;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  margin-top: 4px;
  flex: none;
  border: 2px solid #fff;
  box-shadow: 0 0 0 1px #c3d7ff;

  &.end {
    background: #fff;
    border-width: 3px;
    border-style: solid;
  }
}

.endpoint-line {
  width: 2px;
  height: 18px;
  margin: 2px 0 2px 5px;
  opacity: 0.6;
}

.detail-desc {
  :deep(.ant-descriptions-item-label) {
    width: 72px;
    padding: 8px 10px;
    background: #fafafa;
    font-size: 12px;
    white-space: nowrap;
  }
  :deep(.ant-descriptions-item-content) {
    padding: 8px 10px;
    font-size: 12px;
    color: @text;
  }
}

.detail-footer {
  flex: none;
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-top: 1px solid @border;
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(24px);
  opacity: 0;
}
</style>
