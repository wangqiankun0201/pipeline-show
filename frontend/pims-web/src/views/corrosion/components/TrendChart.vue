<template>
  <div class="trend-chart">
    <a-empty v-if="!prediction" description="暂无预测曲线" class="chart-empty" />
    <v-chart v-else :option="option" autoresize class="chart" />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import '@/utils/echarts'
import type { PredictionVO } from '@/types/corrosion'
import { colors } from '@/styles/theme'

const props = defineProps<{ prediction: PredictionVO | null }>()

/** 情景配色：0.7 青、1.0 主色、1.3 粉 */
const SCENARIO_COLORS: Record<string, string> = { '0.7': '#00A2FA', '1': colors.primary, '1.3': colors.important }

function scenarioColor(n: number, index: number): string {
  return SCENARIO_COLORS[String(Number(n.toFixed(2)))] || ['#2470ff', '#00A2FA', '#fe377e', '#f5c02b'][index % 4]
}

const option = computed(() => {
  const p = props.prediction
  if (!p) return {}
  const wall = p.parameter.wallThicknessMm
  const mainN = p.main.n
  const today = dayjs().format('YYYY-MM-DD')
  const scenarios = p.scenarios.length ? p.scenarios : [p.main]

  const lineSeries = scenarios.map((s, i) => {
    const isMain = s.n === mainN
    const color = scenarioColor(s.n, i)
    return {
      name: `n = ${s.n.toFixed(1)}${isMain ? '（主情景）' : ''}`,
      type: 'line',
      showSymbol: false,
      smooth: false,
      z: isMain ? 5 : 3,
      lineStyle: { width: isMain ? 3 : 2, type: isMain ? 'solid' : 'dashed', color },
      itemStyle: { color },
      areaStyle: isMain ? { color: 'rgba(36, 112, 255, 0.06)' } : undefined,
      data: (s.curve || []).map((pt) => [pt.date, pt.depthMm, pt.tYears]),
      markLine:
        i === 0
          ? {
              silent: true,
              symbol: 'none',
              label: { position: 'insideEndTop', fontSize: 12 },
              data: [
                {
                  yAxis: wall,
                  lineStyle: { color: colors.error, type: 'solid', width: 2 },
                  label: { formatter: `名义壁厚 ${wall} mm`, color: colors.error }
                },
                {
                  yAxis: Number((wall * 0.8).toFixed(3)),
                  lineStyle: { color: colors.warning, type: 'dashed' },
                  label: { formatter: '80% 壁厚', color: colors.warning }
                },
                {
                  xAxis: p.parameter.anchorDate,
                  lineStyle: { color: '#1760E8', type: 'dashed' },
                  label: { formatter: `最近内检 ${p.parameter.anchorDate}`, position: 'insideStartTop', color: '#1760E8' }
                },
                {
                  xAxis: today,
                  lineStyle: { color: '#999', type: 'dotted' },
                  label: { formatter: `今天 ${today}`, position: 'insideEndTop', color: '#999' }
                },
                {
                  xAxis: p.targetDate,
                  lineStyle: { color: colors.success, type: 'dashed' },
                  label: { formatter: `目标 ${p.targetDate}`, position: 'insideMiddleTop', color: colors.success }
                }
              ]
            }
          : undefined
    }
  })

  const normalPoints = p.inspectionPoints.filter((pt) => !pt.isAnchor)
  const anchorPoints = p.inspectionPoints.filter((pt) => pt.isAnchor)

  return {
    color: scenarios.map((s, i) => scenarioColor(s.n, i)),
    grid: { left: 64, right: 40, top: 56, bottom: 72 },
    legend: { top: 8, left: 'center', textStyle: { color: '#666' } },
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'cross', label: { backgroundColor: '#2470ff' } },
      valueFormatter: (v: unknown) => (typeof v === 'number' ? `${v.toFixed(3)} mm` : String(v))
    },
    xAxis: {
      type: 'time',
      name: '日期',
      axisLine: { lineStyle: { color: '#C1C2C1' } },
      axisLabel: { color: '#666', formatter: '{yyyy}' },
      splitLine: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '腐蚀深度（mm）',
      min: 0,
      max: Math.ceil(wall * 1.15),
      nameTextStyle: { color: '#666' },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { color: '#E9EDF0' } }
    },
    dataZoom: [
      { type: 'inside', xAxisIndex: 0 },
      { type: 'slider', xAxisIndex: 0, height: 18, bottom: 16, borderColor: '#E9EDF0' }
    ],
    series: [
      ...lineSeries,
      {
        name: '历史内检',
        type: 'scatter',
        symbolSize: 10,
        z: 8,
        itemStyle: { color: '#fff', borderColor: '#1760E8', borderWidth: 2 },
        data: normalPoints.map((pt) => ({ value: [pt.date, pt.depthMm], depthPct: pt.depthPct }))
      },
      {
        name: '锚定内检',
        type: 'effectScatter',
        symbolSize: 14,
        z: 9,
        rippleEffect: { scale: 2.5, brushType: 'stroke' },
        itemStyle: { color: '#1760E8' },
        data: anchorPoints.map((pt) => ({ value: [pt.date, pt.depthMm], depthPct: pt.depthPct }))
      },
      {
        name: '目标预测点',
        type: 'scatter',
        symbol: 'diamond',
        symbolSize: 14,
        z: 9,
        itemStyle: { color: colors.success, borderColor: '#fff', borderWidth: 2 },
        data: [[p.targetDate, Math.min(p.main.predDepthMm, wall)]]
      }
    ]
  }
})
</script>

<style scoped>
.trend-chart {
  height: 440px;
}

.chart {
  width: 100%;
  height: 100%;
}

.chart-empty {
  padding-top: 140px;
}
</style>
