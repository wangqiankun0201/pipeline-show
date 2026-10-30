<template>
  <div class="risk-matrix">
    <a-empty v-if="!matrix" description="风险矩阵配置未加载" />
    <template v-else>
      <div class="matrix-wrap">
        <div class="axis-y-title">失效可能性 PoF</div>
        <div class="matrix-grid" :style="gridStyle">
          <template v-for="pof in pofDesc" :key="`row-${pof.level}`">
            <div class="axis-y-label">
              <b>{{ pof.level }}</b>
              <span>{{ pof.name }}</span>
            </div>
            <div
              v-for="cof in matrix.cofLevels"
              :key="`cell-${pof.level}-${cof.level}`"
              class="matrix-cell"
              :class="{ hit: pof.level === pofLevel && cof.level === cofLevel }"
              :style="{ background: cellColor(pof.level, cof.level) }"
            >
              <span class="cell-text">{{ cellName(pof.level, cof.level) }}</span>
              <span v-if="pof.level === pofLevel && cof.level === cofLevel" class="cell-pin"><AimOutlined /></span>
            </div>
          </template>
          <div />
          <div v-for="cof in matrix.cofLevels" :key="`x-${cof.level}`" class="axis-x-label">
            <b>{{ cof.level }}</b>
            <span>{{ cof.name }}</span>
          </div>
        </div>
      </div>
      <div class="axis-x-title">失效后果 CoF</div>
      <div class="matrix-legend">
        <span v-for="lv in matrix.riskLevels" :key="lv.code" class="legend-item">
          <i :style="{ background: lv.color }" />{{ lv.name }}
        </span>
      </div>
      <div v-if="!pofLevel || !cofLevel" class="matrix-pending">
        <ClockCircleOutlined /> 待计算：计算模型接入后将在矩阵中定位当前管段
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { AimOutlined, ClockCircleOutlined } from '@ant-design/icons-vue'
import type { RiskMatrixVO } from '@/types/risk'

const props = defineProps<{ matrix: RiskMatrixVO | null; pofLevel: number | null; cofLevel: number | null }>()

/** 纵轴自上而下 PoF 由高到低 */
const pofDesc = computed(() => [...(props.matrix?.pofLevels || [])].sort((a, b) => b.level - a.level))

const gridStyle = computed(() => ({
  gridTemplateColumns: `72px repeat(${props.matrix?.cofLevels.length || 5}, minmax(0, 1fr))`
}))

function cellCode(pof: number, cof: number): string | undefined {
  return props.matrix?.cells?.[pof - 1]?.[cof - 1]
}

function cellColor(pof: number, cof: number): string {
  const code = cellCode(pof, cof)
  const color = props.matrix?.riskLevels.find((l) => l.code === code)?.color || '#E9EDF0'
  const hit = pof === props.pofLevel && cof === props.cofLevel
  return hit ? color : `${color}${props.pofLevel ? '55' : 'cc'}`
}

function cellName(pof: number, cof: number): string {
  const code = cellCode(pof, cof)
  return props.matrix?.riskLevels.find((l) => l.code === code)?.name || ''
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.matrix-wrap {
  display: flex;
  gap: 4px;
}

.axis-y-title {
  writing-mode: vertical-rl;
  transform: rotate(180deg);
  text-align: center;
  font-size: 12px;
  color: @text;
  font-weight: 700;
}

.matrix-grid {
  flex: 1;
  display: grid;
  gap: 3px;
}

.axis-y-label,
.axis-x-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  color: @text-secondary;

  b {
    font-family: @font-number;
    font-size: 16px;
    font-weight: 400;
    color: @text;
  }
}

.axis-y-label {
  flex-direction: row;
  gap: 6px;
  justify-content: flex-end;
  padding-right: 4px;
}

.axis-x-label {
  padding-top: 4px;
}

.matrix-cell {
  position: relative;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
  transition: transform 0.2s;

  &.hit {
    transform: scale(1.06);
    box-shadow:
      0 0 0 3px #fff,
      0 0 0 5px @primary-deep;
    z-index: 1;
  }
}

.cell-text {
  font-size: 12px;
  color: #fff;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.25);
}

.cell-pin {
  position: absolute;
  top: 2px;
  right: 4px;
  color: #fff;
}

.axis-x-title {
  text-align: center;
  font-size: 12px;
  font-weight: 700;
  color: @text;
  margin: 6px 0 10px 76px;
}

.matrix-legend {
  display: flex;
  justify-content: center;
  gap: 16px;
  font-size: 12px;
  color: @text;
}

.legend-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;

  i {
    width: 12px;
    height: 12px;
    border-radius: 2px;
  }
}

.matrix-pending {
  margin-top: 12px;
  padding: 8px 12px;
  text-align: center;
  font-size: 12px;
  color: @warning;
  background: #fff7eb;
  border: 1px dashed #ffebcc;
}
</style>
