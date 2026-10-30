<template>
  <a-form layout="vertical" class="predict-form" :model="form">
    <a-form-item label="预测目标">
      <a-radio-group v-model:value="form.mode" button-style="solid">
        <a-radio-button value="years">内检后年数</a-radio-button>
        <a-radio-button value="date">目标日期</a-radio-button>
      </a-radio-group>
    </a-form-item>
    <a-form-item v-if="form.mode === 'years'" label="内检后年数 Δt（年）" :extra="yearsExtra">
      <a-input-number v-model:value="form.yearsAfterIli" :min="0.1" :max="50" :step="1" :precision="1" class="full" />
    </a-form-item>
    <a-form-item v-else label="目标日期" :extra="anchorDate ? `须晚于最近内检日期 ${anchorDate}` : ''">
      <a-date-picker
        v-model:value="form.targetDate"
        value-format="YYYY-MM-DD"
        :disabled-date="disabledDate"
        class="full"
        placeholder="请选择目标日期"
      />
    </a-form-item>
    <a-form-item label="情景指数 n">
      <div class="n-row">
        <a-select v-model:value="form.nPreset" class="n-select" :options="nOptions" />
        <a-input-number
          v-if="form.nPreset === 'custom'"
          v-model:value="form.customN"
          :min="0.1"
          :max="3"
          :step="0.1"
          :precision="2"
          class="n-custom"
        />
      </div>
    </a-form-item>
    <a-form-item label="曲线延伸（内检后年数）">
      <a-input-number v-model:value="form.curveEndYears" :min="1" :max="50" :precision="0" class="full" placeholder="默认自动" />
    </a-form-item>
    <a-form-item>
      <a-checkbox v-model:checked="form.compareScenarios">同时计算 n=0.7 / 1.0 / 1.3 对比情景</a-checkbox>
    </a-form-item>
    <a-button type="primary" size="large" block :loading="loading" :disabled="disabled" @click="submit">
      <ThunderboltOutlined /> 预测计算
    </a-button>
  </a-form>
</template>

<script setup lang="ts">
import { computed, reactive } from 'vue'
import { message } from 'ant-design-vue'
import { ThunderboltOutlined } from '@ant-design/icons-vue'
import dayjs, { type Dayjs } from 'dayjs'
import type { PredictRequest } from '@/types/corrosion'

const props = defineProps<{ segmentId: number | null; anchorDate?: string | null; loading?: boolean; disabled?: boolean }>()
const emit = defineEmits<{ (e: 'submit', req: PredictRequest): void }>()

const nOptions = [
  { label: 'n = 1.0（基准，常速增长）', value: '1.0' },
  { label: 'n = 0.7（减速情景）', value: '0.7' },
  { label: 'n = 1.3（加速情景）', value: '1.3' },
  { label: '自定义', value: 'custom' }
]

const form = reactive({
  mode: 'years' as 'years' | 'date',
  yearsAfterIli: 5 as number | null,
  targetDate: null as string | null,
  nPreset: '1.0',
  customN: 1.0 as number | null,
  curveEndYears: null as number | null,
  compareScenarios: true
})

const yearsExtra = computed(() => {
  if (!props.anchorDate || !form.yearsAfterIli) return 'Δt 以最近一次内检日期为起点，不是从今天起算'
  const target = dayjs(props.anchorDate).add(Math.round(form.yearsAfterIli * 365.25), 'day')
  return `对应目标日期约 ${target.format('YYYY-MM-DD')}（Δt 自最近内检起算）`
})

function disabledDate(current: Dayjs) {
  return !!props.anchorDate && current.isBefore(dayjs(props.anchorDate).add(1, 'day'), 'day')
}

function submit() {
  if (!props.segmentId) {
    message.warning('请先选择管段')
    return
  }
  const scenarioN = form.nPreset === 'custom' ? Number(form.customN) : Number(form.nPreset)
  if (!scenarioN || scenarioN < 0.1 || scenarioN > 3) {
    message.warning('情景指数 n 须在 0.1 ~ 3.0 之间')
    return
  }
  if (form.mode === 'date' && !form.targetDate) {
    message.warning('请选择目标日期')
    return
  }
  if (form.mode === 'years' && (!form.yearsAfterIli || form.yearsAfterIli <= 0)) {
    message.warning('请输入内检后年数')
    return
  }
  emit('submit', {
    segmentId: props.segmentId,
    targetDate: form.mode === 'date' ? form.targetDate : null,
    yearsAfterIli: form.mode === 'years' ? form.yearsAfterIli : null,
    scenarioN,
    compareScenarios: form.compareScenarios,
    curveEndYears: form.curveEndYears
  })
}

defineExpose({ submit })
</script>

<style scoped lang="less">
.predict-form {
  :deep(.ant-form-item) {
    margin-bottom: 14px;
  }
}

.full {
  width: 100%;
}

.n-row {
  display: flex;
  gap: 8px;
}

.n-select {
  flex: 1;
}

.n-custom {
  width: 100px;
}
</style>
