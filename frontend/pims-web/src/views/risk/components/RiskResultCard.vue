<template>
  <div class="risk-result">
    <div class="result-grid">
      <div class="result-item">
        <div class="result-label">失效可能性 PoF</div>
        <div class="result-value num-font">{{ display(result?.pofValue, result?.pofLevel) }}</div>
        <a-tag v-if="pending" color="orange" class="pending-tag">待接入</a-tag>
        <span v-else class="text-secondary level-text">等级 {{ result?.pofLevel ?? '—' }}</span>
      </div>
      <div class="result-item">
        <div class="result-label">失效后果 CoF</div>
        <div class="result-value num-font">{{ display(result?.cofValue, result?.cofLevel) }}</div>
        <a-tag v-if="pending" color="orange" class="pending-tag">待接入</a-tag>
        <span v-else class="text-secondary level-text">等级 {{ result?.cofLevel ?? '—' }}</span>
      </div>
      <div class="result-item level" :style="{ borderColor: levelColor, background: levelBg }">
        <div class="result-label">风险等级</div>
        <div class="result-value" :style="{ color: levelColor }">{{ result?.riskLevelName || '—' }}</div>
        <a-tag v-if="pending" color="orange" class="pending-tag">待接入</a-tag>
      </div>
    </div>
    <a-alert
      v-if="result?.message"
      :type="result.status === 'CALCULATED' ? 'success' : 'info'"
      show-icon
      :message="result.message"
      class="result-msg"
    />
    <div v-if="result" class="result-meta text-secondary">
      {{ result.segmentName }} · {{ result.assessTime }} · {{ result.statusName }}
    </div>
    <a-empty v-else description="选择管段并点击“风险计算”" :image="simpleImage" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Empty } from 'ant-design-vue'
import type { AssessmentVO, RiskLevelDef } from '@/types/risk'

const props = defineProps<{ result: AssessmentVO | null; riskLevels: RiskLevelDef[] }>()
const simpleImage = Empty.PRESENTED_IMAGE_SIMPLE

const pending = computed(() => !props.result || props.result.status === 'MODEL_PENDING' || props.result.riskLevel === null)

const levelColor = computed(
  () => props.riskLevels.find((l) => l.code === props.result?.riskLevel)?.color || '#C1C2C1'
)
const levelBg = computed(() => `${levelColor.value}14`)

function display(value: number | null | undefined, level: number | null | undefined): string {
  if (value !== null && value !== undefined) return String(value)
  if (level !== null && level !== undefined) return String(level)
  return '—'
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.result-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}

.result-item {
  padding: 12px;
  border: 1px solid #c3d7ff;
  background: linear-gradient(90deg, #e5effe 0%, #f6faff 100%);
  text-align: center;

  &.level {
    background: #fafafa;
  }
}

.result-label {
  font-size: 12px;
  color: @text;
}

.result-value {
  font-size: 30px;
  line-height: 40px;
  color: @primary;
  font-weight: 400;
}

.level .result-value {
  font-size: 22px;
  font-weight: 700;
}

.pending-tag {
  margin-inline-end: 0;
}

.level-text {
  font-size: 12px;
}

.result-msg {
  margin-bottom: 8px;
}

.result-meta {
  font-size: 12px;
  text-align: right;
}
</style>
