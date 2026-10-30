<template>
  <div class="factor-form">
    <a-spin :spinning="!!loading">
      <a-empty v-if="!groups.length && !loading" description="未获取到风险因素定义" />
      <a-form ref="formRef" layout="vertical" :model="values">
        <div class="eval-row">
          <a-form-item label="评价年限（内检后年数）" name="__evaluateYears" class="eval-item">
            <a-input-number v-model:value="evaluateYears" :min="1" :max="50" :precision="0" class="full">
              <template #addonAfter>年</template>
            </a-input-number>
          </a-form-item>
          <a-button :loading="prefilling" :disabled="!segmentId" @click="emit('prefill')">
            <SyncOutlined /> 重新带入数据
          </a-button>
        </div>
        <div v-for="group in groups" :key="group.groupCode" class="factor-group">
          <div class="group-header" :class="`group-${group.groupCode.toLowerCase()}`">
            <span class="group-badge">{{ group.groupCode === 'POF' ? 'PoF' : group.groupCode === 'COF' ? 'CoF' : group.groupCode }}</span>
            <span class="group-name">{{ group.groupName }}</span>
            <span class="group-count text-secondary">{{ group.factors.length }} 项</span>
          </div>
          <div class="factor-grid">
            <a-form-item
              v-for="f in group.factors"
              :key="f.code"
              :name="f.code"
              :rules="f.required ? [{ required: true, message: `请输入${f.name}` }] : []"
            >
              <template #label>
                <span class="factor-label">
                  {{ f.name }}
                  <a-tag :color="SOURCE_COLOR[f.source]" class="source-tag">{{ f.sourceName }}</a-tag>
                </span>
              </template>
              <a-select
                v-if="f.inputType === 'select'"
                v-model:value="values[f.code]"
                :options="f.options || []"
                allow-clear
                placeholder="请选择"
              />
              <a-input-number
                v-else-if="f.inputType === 'number'"
                v-model:value="values[f.code] as number"
                :min="f.min ?? undefined"
                :max="f.max ?? undefined"
                class="full"
                placeholder="请输入"
              >
                <template v-if="f.unit" #addonAfter>{{ f.unit }}</template>
              </a-input-number>
              <a-input v-else v-model:value="values[f.code] as string" :maxlength="100" placeholder="请输入" />
            </a-form-item>
          </div>
        </div>
      </a-form>
    </a-spin>
    <div class="form-actions">
      <a-button @click="reset">重置</a-button>
      <a-button type="primary" size="large" :loading="submitting" :disabled="!segmentId" @click="submit">
        <CalculatorOutlined /> 风险计算
      </a-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue'
import type { FormInstance } from 'ant-design-vue'
import { CalculatorOutlined, SyncOutlined } from '@ant-design/icons-vue'
import type { AssessRequest, FactorSource, FactorValue, RiskFactorGroupVO } from '@/types/risk'

const props = defineProps<{
  groups: RiskFactorGroupVO[]
  segmentId: number | null
  prefillValues: Record<string, FactorValue> | null
  loading?: boolean
  prefilling?: boolean
  submitting?: boolean
}>()
const emit = defineEmits<{ (e: 'submit', req: AssessRequest): void; (e: 'prefill'): void }>()

const SOURCE_COLOR: Record<FactorSource, string> = { CORROSION: 'blue', GIS: 'cyan', MANUAL: 'default' }

const formRef = ref<FormInstance>()
const values = reactive<Record<string, FactorValue>>({})
const evaluateYears = ref<number>(5)

function applyPrefill() {
  Object.keys(values).forEach((k) => delete values[k])
  props.groups.forEach((g) => g.factors.forEach((f) => (values[f.code] = null)))
  if (props.prefillValues) Object.assign(values, props.prefillValues)
}

watch(() => [props.prefillValues, props.groups], applyPrefill, { immediate: true })

function reset() {
  applyPrefill()
  formRef.value?.clearValidate()
}

async function submit() {
  if (!props.segmentId) return
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  emit('submit', { segmentId: props.segmentId, evaluateYears: evaluateYears.value, inputs: { ...values } })
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.eval-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 8px;
}

.eval-item {
  width: 240px;
  margin-bottom: 0;
}

.factor-group {
  border: 1px solid @border;
  margin-top: 12px;
}

.group-header {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: linear-gradient(90deg, #e5effe 0%, #f6faff 100%);
  border-bottom: 1px solid @border;

  &.group-cof {
    background: linear-gradient(90deg, #fff3f4 0%, #fffafa 100%);
    .group-badge {
      background: @error;
    }
  }
}

.group-badge {
  padding: 0 8px;
  line-height: 22px;
  font-size: 12px;
  color: #fff;
  background: @primary;
  border-radius: 2px;
  font-weight: 700;
}

.group-name {
  font-size: 14px;
  font-weight: 700;
  color: @text-main;
}

.group-count {
  margin-left: auto;
  font-size: 12px;
}

.factor-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 16px;
  padding: 12px 14px 0;

  :deep(.ant-form-item) {
    margin-bottom: 14px;
  }
}

.factor-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
}

.source-tag {
  margin-inline-end: 0;
  font-size: 11px;
  line-height: 18px;
  padding: 0 4px;
}

.full {
  width: 100%;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid @border;
}
</style>
