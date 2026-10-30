<template>
  <PageCard title="管段风险清单" :sub-title="`共 ${segments.length} 段`" no-padding>
    <a-spin :spinning="!!loading">
      <a-empty v-if="!loading && !segments.length" description="暂无管段" class="list-empty" />
      <div class="risk-list">
        <div
          v-for="item in segments"
          :key="item.segmentId"
          class="risk-item"
          :class="{ active: item.segmentId === selectedId }"
          @click="emit('select', item.segmentId)"
        >
          <div class="risk-level-bar" :style="{ background: levelColor(item.riskLevel) }" />
          <div class="risk-item-main">
            <div class="risk-item-top">
              <span class="risk-name">{{ item.segmentName }}</span>
              <a-tag :color="statusColor(item.status)" class="status-tag">{{ item.statusName }}</a-tag>
            </div>
            <div class="risk-item-sub">
              <span>{{ item.mine }} · {{ item.pipelineName }}</span>
            </div>
            <div class="risk-item-metrics">
              <span>
                预测深度比
                <b :style="{ color: flagColor(item.corrosionLimitFlag) }">{{ fmtRatio(item.depthRatio, 1) }}</b>
              </span>
              <span>
                风险等级
                <b :style="{ color: levelColor(item.riskLevel) }">{{ item.riskLevelName || '待计算' }}</b>
              </span>
            </div>
          </div>
        </div>
      </div>
    </a-spin>
  </PageCard>
</template>

<script setup lang="ts">
import PageCard from '@/components/PageCard.vue'
import type { LimitFlag } from '@/types/corrosion'
import type { RiskLevelDef, RiskSegmentVO } from '@/types/risk'
import { colors } from '@/styles/theme'
import { fmtRatio } from '@/utils/format'

const props = defineProps<{
  segments: RiskSegmentVO[]
  selectedId: number | null
  riskLevels: RiskLevelDef[]
  loading?: boolean
}>()
const emit = defineEmits<{ (e: 'select', segmentId: number): void }>()

function levelColor(code: string | null): string {
  return props.riskLevels.find((l) => l.code === code)?.color || colors.disabled
}

function flagColor(flag: LimitFlag | null): string {
  if (flag === 'MODEL_OUT_OF_RANGE') return colors.error
  if (flag === 'ATTENTION') return colors.warning
  if (flag === 'NORMAL') return colors.success
  return colors.textSecondary
}

function statusColor(status: string): string {
  if (status === 'CALCULATED') return 'green'
  if (status === 'FAILED') return 'red'
  return 'orange'
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.list-empty {
  padding: 48px 0;
}

.risk-list {
  max-height: 720px;
  overflow-y: auto;
  padding: 8px 0;
}

.risk-item {
  display: flex;
  margin: 4px 12px;
  border: 1px solid @border;
  border-radius: 2px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fff;

  &:hover {
    border-color: #c3d7ff;
    background: #f5f8ff;
  }

  &.active {
    border-color: @primary;
    background: #eff4ff;
  }
}

.risk-level-bar {
  width: 4px;
  flex: none;
}

.risk-item-main {
  flex: 1;
  padding: 10px 12px;
  min-width: 0;
}

.risk-item-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.risk-name {
  font-size: 14px;
  font-weight: 700;
  color: @text-main;
}

.status-tag {
  margin-inline-end: 0;
}

.risk-item-sub {
  font-size: 12px;
  color: @text-secondary;
  margin: 2px 0 6px;
}

.risk-item-metrics {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: @text;

  b {
    margin-left: 4px;
  }
}
</style>
