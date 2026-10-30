<template>
  <div class="prediction-result">
    <a-empty v-if="!prediction" description="设置预测条件后点击“预测计算”" class="result-empty" />
    <template v-else>
      <div class="result-head">
        <span>
          目标日期 <b class="num-font">{{ prediction.targetDate }}</b>
          <span class="text-secondary">（内检后 Δt = {{ fmtNum(prediction.deltaYears, 2) }} 年，n = {{ fmtNum(main.n, 2) }}）</span>
        </span>
        <LimitFlagTag :flag="main.limitFlag" :name="main.limitFlagName" />
      </div>
      <div class="kpi-grid">
        <div class="kpi" :class="toneClass">
          <div class="kpi-label">预测腐蚀深度</div>
          <div class="kpi-value num-font">{{ fmtNum(main.predDepthMm, 3) }}<i>mm</i></div>
        </div>
        <div class="kpi" :class="toneClass">
          <div class="kpi-label">深度比（占壁厚）</div>
          <div class="kpi-value num-font">{{ fmtRatio(main.depthRatio, 1) }}</div>
          <a-progress
            :percent="Math.min(100, Math.round(main.depthRatio * 1000) / 10)"
            :show-info="false"
            :stroke-color="flagColor"
            size="small"
          />
        </div>
        <div class="kpi">
          <div class="kpi-label">剩余壁厚</div>
          <div class="kpi-value num-font">{{ fmtNum(Math.max(0, main.remainingWallMm), 3) }}<i>mm</i></div>
        </div>
        <div class="kpi">
          <div class="kpi-label">达到名义壁厚日期</div>
          <div class="kpi-value num-font small">{{ main.outOfRangeDate || '预测期内未达到' }}</div>
        </div>
      </div>

      <a-table
        v-if="prediction.scenarios.length > 1"
        :data-source="prediction.scenarios"
        :columns="scenarioColumns"
        :pagination="false"
        size="small"
        row-key="n"
        class="scenario-table"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'n'">
            <span :class="{ 'main-n': record.n === main.n }">n = {{ fmtNum(record.n, 1) }}</span>
          </template>
          <template v-else-if="column.key === 'k'">{{ fmtNum(record.kValue, 4) }}</template>
          <template v-else-if="column.key === 'depth'">{{ fmtNum(record.predDepthMm, 3) }}</template>
          <template v-else-if="column.key === 'ratio'">{{ fmtRatio(record.depthRatio, 1) }}</template>
          <template v-else-if="column.key === 'flag'">
            <LimitFlagTag :flag="record.limitFlag" :name="record.limitFlagName" />
          </template>
          <template v-else-if="column.key === 'date'">{{ record.outOfRangeDate || '—' }}</template>
        </template>
      </a-table>

      <a-alert
        v-for="(w, i) in prediction.warnings"
        :key="i"
        type="warning"
        show-icon
        :message="w"
        class="warn-item"
      />
      <div class="calc-meta text-secondary">计算编号 {{ prediction.calcId }} · {{ prediction.calcTime }}</div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { TableColumnType } from 'ant-design-vue'
import LimitFlagTag from '@/components/LimitFlagTag.vue'
import type { PredictionVO, ScenarioResultVO } from '@/types/corrosion'
import { colors } from '@/styles/theme'
import { fmtNum, fmtRatio } from '@/utils/format'

const props = defineProps<{ prediction: PredictionVO | null }>()

const main = computed(() => props.prediction?.main as ScenarioResultVO)

const flagColor = computed(() => {
  const flag = props.prediction?.main.limitFlag
  if (flag === 'MODEL_OUT_OF_RANGE') return colors.error
  if (flag === 'ATTENTION') return colors.warning
  return colors.success
})

const toneClass = computed(() => {
  const flag = props.prediction?.main.limitFlag
  if (flag === 'MODEL_OUT_OF_RANGE') return 'tone-error'
  if (flag === 'ATTENTION') return 'tone-warning'
  return 'tone-normal'
})

const scenarioColumns: TableColumnType[] = [
  { title: '情景', key: 'n', align: 'center' },
  { title: 'k', key: 'k', align: 'right' },
  { title: '预测深度(mm)', key: 'depth', align: 'right' },
  { title: '深度比', key: 'ratio', align: 'right' },
  { title: '状态', key: 'flag', align: 'center' },
  { title: '达到壁厚', key: 'date', align: 'center' }
]
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.result-empty {
  padding: 48px 0;
}

.result-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
  font-size: 14px;

  b {
    font-size: 18px;
    color: @primary;
    font-weight: 400;
  }
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.kpi {
  padding: 12px 14px;
  background: linear-gradient(90deg, #e5effe 0%, #f6faff 100%);
  border: 1px solid #c3d7ff;
  min-width: 0;

  &.tone-warning {
    background: linear-gradient(90deg, #fff7eb 0%, #fffaf2 100%);
    border-color: #ffebcc;
    .kpi-value {
      color: @warning;
    }
  }

  &.tone-error {
    background: linear-gradient(90deg, #fff3f4 0%, #fff8f8 100%);
    border-color: #ffa39e;
    .kpi-value {
      color: @error;
    }
  }
}

.kpi-label {
  font-size: 12px;
  color: @text;
}

.kpi-value {
  font-size: 28px;
  line-height: 36px;
  color: @primary;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &.small {
    font-size: 20px;
  }

  i {
    font-style: normal;
    font-family: 'Microsoft YaHei', sans-serif;
    font-size: 12px;
    color: @text-secondary;
    margin-left: 4px;
  }
}

.scenario-table {
  margin-bottom: 12px;
}

.main-n {
  color: @primary;
  font-weight: 700;
}

.warn-item {
  margin-bottom: 8px;
}

.calc-meta {
  font-size: 12px;
  text-align: right;
}
</style>
