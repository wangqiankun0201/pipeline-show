<template>
  <div class="report-charts">
    <section class="analysis-panel">
      <header class="panel-heading">
        <h3>计算腐蚀深度与实测深度对比</h3>
        <div class="legend"><span><i class="legend-line" />计算深度</span><span><i class="legend-dot" />实测深度</span></div>
      </header>
      <div class="comparison-layout">
        <div class="plot-wrap"><v-chart :option="comparisonOption" autoresize class="comparison-chart" /></div>
        <div class="comparison-data">
          <a-collapse ghost>
            <a-collapse-panel key="comparison" header="查看实测与计算明细">
          <a-table :data-source="comparisonRows" :columns="comparisonColumns" row-key="date" :pagination="false" size="small">
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'date'"><strong>{{ record.date }}</strong><span class="cell-caption">{{ record.pointType }}</span></template>
            </template>
          </a-table>
            </a-collapse-panel>
          </a-collapse>
        </div>
      </div>
    </section>
    <section class="analysis-panel">
      <header class="panel-heading trend-heading">
        <h3>投运至2031年腐蚀深度变化</h3>
        <a-segmented v-model:value="range" :options="rangeOptions" />
      </header>
      <div class="trend-body">
        <div class="threshold-caption"><span class="threshold-rule" />壁厚 {{ report.wallThicknessMm }} mm<span class="cutoff-caption">达到壁厚日期 {{ report.cutoffDate }}</span></div>
        <v-chart :option="trendOption" autoresize class="trend-chart" />
        <div class="annual-strip" aria-label="2026至2031年末预测深度">
          <div v-for="item in report.annual" :key="item.year" class="annual-cell" :class="{ 'is-stopped': item.stopped }">
            <span class="annual-year">{{ item.year }}</span>
            <div class="annual-depth">{{ item.depthMm == null ? '—' : item.depthMm.toFixed(4) }}<span v-if="item.depthMm != null">mm</span></div>
            <span class="annual-state">{{ item.stopped ? '已达壁厚 · 截止' : '12月31日' }}</span>
          </div>
        </div>
        <a-collapse ghost class="curve-table">
          <a-collapse-panel key="curve" header="查看完整曲线数据">
            <a-table :data-source="report.curve" :columns="curveColumns" row-key="date" :pagination="{ pageSize: 10, showSizeChanger: false }" size="small" />
          </a-collapse-panel>
        </a-collapse>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import VChart from 'vue-echarts'
import type { PowerLawReport } from '@/types/corrosion'
import '@/utils/echarts'

const props = defineProps<{ report: PowerLawReport }>()
const range = ref('all')
const rangeOptions = [{ label: '完整周期', value: 'all' }, { label: '2026—2031年', value: 'forecast' }]
const calculatedColor = '#2a78d6'
const measuredColor = '#eb6834'
const numberCell = ({ text }: { text: number }) => Number(text).toFixed(4)
const comparisonRows = computed(() => [
  { date: props.report.commissionDate, tYears: 0, measuredDepthMm: 0, calculatedDepthMm: 0, errorMm: 0, pointType: '投运初始条件' },
  ...props.report.comparison.map(point => ({ ...point, pointType: '内检实测' }))
])
const comparisonColumns = [
  { title: '日期', key: 'date', width: 120 },
  { title: '实测 / mm', dataIndex: 'measuredDepthMm', customRender: numberCell, align: 'right' as const },
  { title: '计算 / mm', dataIndex: 'calculatedDepthMm', customRender: numberCell, align: 'right' as const },
  { title: '偏差 / mm', dataIndex: 'errorMm', customRender: numberCell, align: 'right' as const }
]
const curveColumns = [
  { title: '日期', dataIndex: 'date' },
  { title: '投运后年数 t', dataIndex: 'tYears', customRender: numberCell },
  { title: '腐蚀深度（mm）', dataIndex: 'depthMm', customRender: numberCell }
]
const axisStyle = {
  axisLabel: { color: '#75849a', fontSize: 12, margin: 14 },
  axisTick: { show: false },
  axisLine: { show: false },
  splitLine: { lineStyle: { color: '#e8eef5', width: 1 } }
}
const tooltip = {
  trigger: 'axis',
  backgroundColor: '#fff', borderColor: '#e5ebf3', borderWidth: 1, padding: [12, 16],
  textStyle: { color: '#34445b', fontSize: 12 },
  extraCssText: 'box-shadow:0 8px 28px rgba(30,55,90,.12);border-radius:6px;',
  axisPointer: { type: 'line', lineStyle: { color: '#b7c6dc', type: 'dashed' } },
  valueFormatter: (value: number) => `${Number(value).toFixed(4)} mm`,
  confine: true
}
const comparisonOption = computed(() => ({
  animation: false,
  color: [calculatedColor, measuredColor],
  tooltip,
  grid: { top: 42, left: 62, right: 92, bottom: 46 },
  xAxis: { type: 'time', min: props.report.commissionDate, ...axisStyle, splitLine: { show: false }, axisLabel: { color: '#75849a', fontSize: 12, formatter: '{yyyy}', hideOverlap: true } },
  yAxis: { type: 'value', name: '深度 / mm', nameTextStyle: { color: '#75849a', align: 'left', padding: [0, 0, 8, -36] }, min: 0, splitNumber: 4, ...axisStyle },
  series: [
    { name: '计算深度', type: 'line', symbol: 'circle', symbolSize: 8, lineStyle: { width: 2 }, itemStyle: { borderColor: '#fff', borderWidth: 2 }, endLabel: { show: true, formatter: '计算', color: '#53627a', offset: [0, -13], fontSize: 11 }, data: comparisonRows.value.map(p => [p.date, p.calculatedDepthMm]) },
    { name: '实测深度', type: 'line', symbol: 'diamond', symbolSize: 10, lineStyle: { width: 2, type: 'dashed' }, itemStyle: { borderColor: '#fff', borderWidth: 2 }, endLabel: { show: true, formatter: '实测', color: '#53627a', offset: [0, 13], fontSize: 11 }, data: comparisonRows.value.map(p => [p.date, p.measuredDepthMm]) }
  ]
}))
const trendOption = computed(() => {
  const report = props.report
  const focus = range.value === 'forecast'
  return {
    animation: false,
    tooltip,
    grid: { top: 42, left: 62, right: 112, bottom: 74 },
    xAxis: { type: 'time', min: focus ? '2026-01-01' : report.commissionDate, max: '2031-12-31', ...axisStyle, splitLine: { show: false }, axisLabel: { color: '#7b8799', formatter: '{yyyy}', margin: 16, hideOverlap: true } },
    yAxis: { type: 'value', name: '深度 / mm', nameTextStyle: { color: '#75849a', align: 'left', padding: [0, 0, 8, -38] }, min: 0, max: report.wallThicknessMm * 1.12, splitNumber: 4, ...axisStyle },
    dataZoom: [{ type: 'inside', filterMode: 'none' }, { type: 'slider', bottom: 10, height: 22, filterMode: 'none', borderColor: 'transparent', backgroundColor: '#f4f7fb', fillerColor: 'rgba(42,120,214,.09)', handleStyle: { color: '#fff', borderColor: '#afc6e4' }, dataBackground: { lineStyle: { color: '#b2c9e7' }, areaStyle: { color: '#e8eff9' } }, selectedDataBackground: { lineStyle: { color: '#2a78d6' }, areaStyle: { color: '#dce8f8' } }, textStyle: { color: '#77869c' }, showDetail: false, brushSelect: false }],
    series: [{
      name: '计算深度', type: 'line', showSymbol: false, smooth: false,
      lineStyle: { width: 2, color: calculatedColor, cap: 'round' }, itemStyle: { color: calculatedColor },
      areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(42,120,214,.12)' }, { offset: 1, color: 'rgba(42,120,214,.01)' }] } },
      endLabel: { show: true, formatter: '计算深度', color: '#53627a', fontSize: 11, offset: [6, 0] },
      data: report.curve.map(p => [p.date, p.depthMm]),
      markArea: { silent: true, itemStyle: { color: 'rgba(42,120,214,0.035)' }, label: { show: !focus, color: '#8492a6', position: 'insideBottom', fontSize: 11 }, data: [[{ name: '2026—2031', xAxis: '2026-01-01' }, { xAxis: '2031-12-31' }]] },
      markLine: { silent: true, symbol: 'none', lineStyle: { color: '#9ba8b9', type: 'dashed', width: 1 }, label: { color: '#66758b', formatter: `壁厚 ${report.wallThicknessMm} mm`, position: 'insideEndTop', fontSize: 11 }, data: [{ yAxis: report.wallThicknessMm }] },
      markPoint: report.cutoffDate <= '2031-12-31' ? { symbol: 'circle', symbolSize: 9, itemStyle: { color: calculatedColor, borderColor: '#fff', borderWidth: 2 }, label: { show: true, position: 'bottom', color: '#53627a', fontSize: 11, lineHeight: 18, backgroundColor: '#fff', borderColor: '#dce6f2', borderWidth: 1, borderRadius: 6, padding: [8, 12], formatter: `达到壁厚\n${report.cutoffDate}`, offset: [-30, 12] }, data: [{ coord: [report.cutoffDate, report.wallThicknessMm] }] } : undefined
    }]
  }
})
</script>

<style scoped>
.report-charts { --ink: #20334d; --muted: #75849a; display: flex; flex-direction: column; gap: 24px; }
.analysis-panel { min-width: 0; background: #fff; border: 1px solid #e2e9f2; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 20px rgba(36,62,102,.035); }
.panel-heading { min-height: 76px; padding: 22px 30px; display: flex; align-items: center; justify-content: space-between; gap: 20px; }
h3 { margin: 0; color: var(--ink); font-size: 18px; font-weight: 600; letter-spacing: .3px; }
.legend { display: flex; align-items: center; gap: 22px; font-size: 12px; color: #52647d; }
.legend > span { display: inline-flex; align-items: center; gap: 9px; white-space: nowrap; }
.legend-line { width: 24px; height: 2px; background: #2a78d6; position: relative; }
.legend-line::after { content: ''; position: absolute; width: 6px; height: 6px; border-radius: 50%; background: #2a78d6; left: 9px; top: -2px; }
.legend-dot { width: 8px; height: 8px; background: #eb6834; transform: rotate(45deg); }
.comparison-layout { display: flex; flex-direction: column; }
.plot-wrap { min-width: 0; width: 100%; padding: 0 22px 8px; }
.comparison-chart { height: 350px; width: 100%; }
.comparison-data { margin: 0 30px; border-top: 1px solid #edf1f7; }
.cell-caption { display: block; color: var(--muted); font-size: 11px; margin-top: 4px; }
.comparison-data strong { font-size: 12px; font-weight: 500; color: #34445b; }
.trend-body { padding: 0 30px 10px; }
.threshold-caption { display: flex; align-items: center; gap: 8px; color: #66758b; font-size: 12px; padding: 2px 0 0; }
.threshold-rule { width: 20px; border-top: 1px dashed #9ba8b9; }
.cutoff-caption { margin-left: auto; color: #75849a; }
.trend-chart { height: 420px; width: 100%; }
.annual-strip { display: grid; grid-template-columns: repeat(6,minmax(0,1fr)); background: #f5f8fc; border: 1px solid #e7edf5; border-radius: 8px; margin: 22px 0 16px; overflow: hidden; }
.annual-cell { padding: 20px 18px; position: relative; }
.annual-cell + .annual-cell::before { content: ''; position: absolute; top: 22px; bottom: 22px; left: 0; width: 1px; background: #e0e7f1; }
.annual-year { color: #52647d; font-size: 12px; font-weight: 600; }
.annual-depth { color: #20334d; font-size: 23px; font-weight: 500; margin: 10px 0 6px; letter-spacing: -.5px; }
.annual-depth > span { font-size: 11px; font-weight: 400; color: #75849a; margin-left: 5px; letter-spacing: 0; }
.annual-state { color: #75849a; font-size: 11px; }
.is-stopped .annual-depth { color: #a4afbd; }
.is-stopped .annual-state { color: #946116; }
.curve-table { border-top: 1px solid #edf1f7; }
:deep(.ant-collapse-header) { color: #667b98 !important; font-size: 12px; padding: 16px 0 !important; }
:deep(.ant-collapse-content-box) { padding: 0 0 16px !important; }
:deep(.ant-table) { font-size: 12px; color: #53627a; }
:deep(.ant-table-thead > tr > th) { background: #f5f8fc !important; color: #75849a; font-size: 12px; font-weight: 500; border-bottom: 1px solid #eaf0f7; padding: 12px !important; }
:deep(.ant-table-tbody > tr > td) { padding: 14px 12px !important; border-bottom: 1px solid #eef2f7; font-variant-numeric: tabular-nums; }
:deep(.ant-table-tbody > tr:hover > td) { background: #f8faff !important; }
:deep(.ant-segmented) { background: #edf2f8; padding: 4px; border-radius: 7px; font-size: 12px; color: #708098; }
:deep(.ant-segmented-item-selected) { color: #245eaa; font-weight: 500; box-shadow: 0 2px 5px rgba(32,65,108,.09); }
@media(max-width: 1200px) { .annual-depth { font-size: 20px; } .annual-cell { padding: 18px 12px; } }
@media(max-width: 850px) { .annual-strip { grid-template-columns: repeat(3,minmax(0,1fr)); } .annual-cell:nth-child(4)::before { display: none; } .annual-cell:nth-child(n+4) { border-top: 1px solid #e0e7f1; } }
@media(max-width: 700px) { .panel-heading { padding: 20px; align-items: flex-start; flex-direction: column; } h3 { font-size: 16px; } .plot-wrap { padding: 0; } .trend-body { padding: 0 12px 4px; } .comparison-data { margin: 0 16px; overflow-x: auto; } .cutoff-caption { margin-left: 0; } .threshold-caption { flex-wrap: wrap; } .comparison-chart { height: 300px; } }
@media(prefers-reduced-motion: reduce) { :deep(*) { transition: none !important; } }
</style>
