<template>
  <PageCard title="模型参数" :sub-title="parameter ? `${parameter.modelName} ${parameter.modelVersion}` : ''">
    <template #extra>
      <GradeTag v-if="parameter" :grade="parameter.grade" :name="parameter.gradeName" />
    </template>
    <a-spin :spinning="!!loading">
      <a-empty v-if="!parameter" :description="emptyText || '请选择可计算的管段'" />
      <template v-else>
        <div class="formula">
          <span class="formula-main">d̂(T+Δt) = d₀ · (1 + Δt / T)<sup>n</sup></span>
          <span class="formula-sub">k = d₀ / T<sup>n</sup>，d₀ = 壁厚 × 最大深度%</span>
        </div>
        <div class="param-grid">
          <div class="param-item">
            <div class="param-label">d₀ 锚定深度</div>
            <div class="param-value num-font">{{ fmtNum(parameter.d0Mm, 3) }}<i>mm</i></div>
          </div>
          <div class="param-item">
            <div class="param-label">T 作用时间</div>
            <div class="param-value num-font">{{ fmtNum(parameter.tYears, 2) }}<i>年</i></div>
          </div>
          <div class="param-item">
            <div class="param-label">k 发展系数</div>
            <div class="param-value num-font">{{ fmtNum(parameter.kValue, 4) }}<i>{{ parameter.kUnit }}</i></div>
          </div>
          <div class="param-item">
            <div class="param-label">n 增长指数</div>
            <div class="param-value num-font">{{ fmtNum(parameter.n, 2) }}</div>
          </div>
        </div>
        <a-descriptions :column="2" size="small" class="param-desc">
          <a-descriptions-item label="名义壁厚">{{ fmtNum(parameter.wallThicknessMm, 1) }} mm</a-descriptions-item>
          <a-descriptions-item label="最大深度">{{ fmtNum(parameter.maxDepthPct, 0) }}%</a-descriptions-item>
          <a-descriptions-item label="投运日期">{{ parameter.commissionDate }}</a-descriptions-item>
          <a-descriptions-item label="锚定内检">{{ parameter.anchorDate }}</a-descriptions-item>
          <a-descriptions-item label="时间原点" :span="2">
            <a-tag color="orange">{{ parameter.timeOriginName }}</a-tag>
          </a-descriptions-item>
          <a-descriptions-item label="参数来源" :span="2">
            <span class="source-text">{{ parameter.parameterSource }}</span>
          </a-descriptions-item>
        </a-descriptions>
      </template>
    </a-spin>
  </PageCard>
</template>

<script setup lang="ts">
import PageCard from '@/components/PageCard.vue'
import GradeTag from '@/components/GradeTag.vue'
import type { ModelParameterVO } from '@/types/corrosion'
import { fmtNum } from '@/utils/format'

defineProps<{ parameter: ModelParameterVO | null; loading?: boolean; emptyText?: string | null }>()
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.formula {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 10px 14px;
  margin-bottom: 14px;
  background: #f5f8ff;
  border-left: 3px solid @primary;
}

.formula-main {
  font-size: 16px;
  font-weight: 700;
  color: @primary-deep;
  font-family: 'Cambria Math', 'Times New Roman', serif;
}

.formula-sub {
  font-size: 12px;
  color: @text-secondary;
}

.param-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.param-item {
  padding: 10px 12px;
  border: 1px solid #c3d7ff;
  background: #fff;
}

.param-label {
  font-size: 12px;
  color: @text;
}

.param-value {
  font-size: 24px;
  color: @primary;
  line-height: 32px;

  i {
    font-style: normal;
    font-family: 'Microsoft YaHei', sans-serif;
    font-size: 12px;
    color: @text-secondary;
    margin-left: 4px;
  }
}

.param-desc {
  :deep(.ant-descriptions-item-label) {
    font-size: 12px;
    color: @text-secondary;
  }
  :deep(.ant-descriptions-item-content) {
    font-size: 12px;
    color: @text;
  }
}

.source-text {
  word-break: break-all;
}
</style>
