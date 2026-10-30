<template>
  <div class="page-container gamma-page">
    <header class="risk-header"><h2>失效风险计算</h2><a-tag color="blue">腐蚀穿透可靠度分析</a-tag></header>
    <section class="controls">
      <a-form layout="inline" @finish="calculate">
        <a-form-item label="管段"><a-select v-model:value="segmentId" :options="options" style="width: 180px" :disabled="loading" @change="calculate" /></a-form-item>
        <a-form-item label="Gamma尺度 θ（mm）"><a-input-number v-model:value="theta" :min="0.001" :max="5" :step="0.05" :disabled="loading" /></a-form-item>
        <a-form-item label="模拟次数"><a-select v-model:value="sampleCount" :options="sampleOptions" style="width: 115px" :disabled="loading" /></a-form-item>
        <a-form-item label="随机种子"><a-input-number v-model:value="seed" :min="0" :max="2147483647" :precision="0" :disabled="loading" /></a-form-item>
        <a-form-item><a-button type="primary" html-type="button" :loading="loading" :disabled="!segmentId" @click="calculate">运行分析</a-button></a-form-item>
      </a-form>
    </section>
    <a-alert v-if="error" :message="error" type="error" show-icon />
    <a-alert message="结果为给定Gamma退化模型和最新内检状态下的累计腐蚀穿透概率，不代表管段综合事故概率。" type="info" show-icon />
    <a-spin :spinning="loading" tip="正在模拟腐蚀增长路径…">
      <template v-if="report">
        <div class="result-heading">{{ report.segmentName }}<span>θ = {{ report.theta }} mm · {{ report.sampleCount.toLocaleString() }} 次模拟 · 种子 {{ report.seed }}</span></div>
        <div class="anchor-strip">
          <div><span>最新内检</span><strong>{{ report.inspectionDate }}</strong></div>
          <div><span>实测腐蚀深度</span><strong>{{ report.inspectionDepthMm }} <small>mm</small></strong></div>
          <div><span>原始壁厚</span><strong>{{ report.wallThicknessMm }} <small>mm</small></strong></div>
          <div><span>内检时剩余壁厚</span><strong>{{ (report.wallThicknessMm - report.inspectionDepthMm).toFixed(3) }} <small>mm</small></strong></div>
        </div>
        <a-alert v-for="warning in report.warnings" :key="warning" :message="warning" type="warning" show-icon class="model-warning" />
        <section v-if="report.annual.length" class="analysis-panel">
          <header><h3>腐蚀深度演化</h3><span>最新内检后条件均值</span></header>
          <v-chart :option="depthOption" autoresize class="chart" />
        </section>
        <section v-if="report.annual.length" class="analysis-panel">
          <header><h3>时变累计腐蚀穿透概率</h3><div class="legend"><span><i class="blue" />Gamma解析值</span><span><i class="orange" />蒙特卡洛</span></div></header>
          <v-chart :option="probabilityOption" autoresize class="chart" />
          <div class="table-wrap"><a-table :data-source="report.annual" :columns="columns" row-key="year" :pagination="false" :scroll="{ x: 1000 }" size="small" /></div>
        </section>
        <section class="analysis-panel details">
          <a-collapse ghost>
            <a-collapse-panel key="history" header="两轮内检回代对比"><a-table :data-source="report.history" :columns="historyColumns" row-key="date" :pagination="false" size="small" /></a-collapse-panel>
            <a-collapse-panel key="convergence" header="蒙特卡洛收敛性检查"><a-table :data-source="report.convergence" :columns="convergenceColumns" :row-key="convergenceKey" :pagination="false" size="small" /></a-collapse-panel>
          </a-collapse>
        </section>
      </template>
      <a-empty v-else-if="!loading" description="请选择管段并运行分析" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import VChart from 'vue-echarts'
import { riskApi } from '@/api/risk'
import { corrosionApi } from '@/api/corrosion'
import type { GammaReport } from '@/types/gamma-risk'
import '@/utils/echarts'

const route = useRoute()
const router = useRouter()
const segmentId = ref<number>()
const options = ref<{ label: string; value: number }[]>([])
const theta = ref(0.2)
const sampleCount = ref(100000)
const seed = ref(20261029)
const sampleOptions = [10000, 50000, 100000, 200000].map(value => ({ value, label: value.toLocaleString() }))
const report = ref<GammaReport | null>(null)
const loading = ref(false)
const error = ref('')
const percent = (value: number) => value === 0 ? '0%' : value < 0.000001 ? `${(value * 100).toExponential(3)}%` : `${(value * 100).toFixed(4)}%`
const convergenceKey = (row: { year: number; sampleCount: number }) => `${row.year}-${row.sampleCount}`
const num = ({ text }: { text: number }) => text.toFixed(4)
const pct = ({ text }: { text: number }) => percent(text)
const columns = [
  { title: '年末', dataIndex: 'year', width: 75 },
  { title: '均值深度 / mm', dataIndex: 'meanDepthMm', customRender: num },
  { title: '解析穿透概率', dataIndex: 'probability', customRender: pct },
  { title: '模拟穿透概率', dataIndex: 'monteCarloProbability', customRender: pct },
  { title: '穿透样本数', dataIndex: 'failedSamples' },
  { title: '模拟标准误差', dataIndex: 'standardError', customRender: pct },
  { title: '模拟95%区间', key: 'interval', customRender: ({ record }: { record: { confidenceLow: number; confidenceHigh: number } }) => `${percent(record.confidenceLow)} ～ ${percent(record.confidenceHigh)}` },
  { title: '抽样说明', key: 'note', customRender: ({ record }: { record: { failedSamples: number; zeroFailureUpper95: number | null } }) => record.failedSamples === 0 ? `本次未观测到穿透；95%上限 ${percent(record.zeroFailureUpper95 ?? 0)}` : '已观测到穿透样本' }
]
const historyColumns = [{ title: '内检日期', dataIndex: 'date' }, { title: '实测深度 / mm', dataIndex: 'measuredDepthMm', customRender: num }, { title: '幂律计算 / mm', dataIndex: 'calculatedDepthMm', customRender: num }]
const convergenceColumns = [{ title: '年末', dataIndex: 'year' }, { title: '模拟次数', dataIndex: 'sampleCount' }, { title: '模拟概率', dataIndex: 'probability', customRender: pct }, { title: '标准误差', dataIndex: 'standardError', customRender: pct }]
const tooltip = { trigger: 'axis', confine: true, backgroundColor: '#fff', borderColor: '#e1e8f2', textStyle: { color: '#34445b' }, axisPointer: { type: 'line', lineStyle: { color: '#b5c6df', type: 'dashed' } } }
const axis = { axisLine: { show: false }, axisTick: { show: false }, axisLabel: { color: '#75849a', margin: 14 }, splitLine: { lineStyle: { color: '#edf1f6' } } }
const depthOption = computed(() => {
  const r = report.value
  if (!r || !r.annual.length) return {}
  //均值到达壁厚补交点，然后停止物理深度曲线；概率仍使用未截断分布。
  const points: [string, number][] = [[r.inspectionDate, r.inspectionDepthMm]]
  const limitT = Math.pow((r.wallThicknessMm - r.inspectionDepthMm) / r.kValue + Math.pow((Date.parse(`${r.inspectionDate}T00:00:00Z`) - Date.parse(`${r.commissionDate}T00:00:00Z`)) / 86400000 / 365.2422, r.n), 1 / r.n)
  const limitDate = new Date(Date.parse(`${r.commissionDate}T00:00:00Z`) + Math.round(limitT * 365.2422) * 86400000).toISOString().slice(0, 10)
  for (const p of r.annual) { if (p.meanDepthMm >= r.wallThicknessMm) { points.push([limitDate, r.wallThicknessMm]); break } points.push([p.date, p.meanDepthMm]) }
  return {
    animation: false, tooltip: { ...tooltip, valueFormatter: (v: number) => `${v.toFixed(4)} mm` }, grid: { left: 64, right: 100, top: 35, bottom: 48 },
    xAxis: { type: 'time', min: r.inspectionDate, max: '2031-12-31', ...axis, splitLine: { show: false }, axisLabel: { color: '#75849a', formatter: '{yyyy}' } },
    yAxis: { type: 'value', min: 0, max: r.wallThicknessMm * 1.12, name: '深度 / mm', ...axis },
    series: [{ name: '条件均值深度', type: 'line', data: points, showSymbol: true, symbolSize: 8, lineStyle: { width: 2, color: '#2a78d6' }, itemStyle: { color: '#2a78d6', borderColor: '#fff', borderWidth: 2 }, areaStyle: { color: 'rgba(42,120,214,.06)' }, markLine: { symbol: 'none', silent: true, lineStyle: { color: '#98a6ba', type: 'dashed' }, label: { formatter: `壁厚 ${r.wallThicknessMm} mm`, color: '#66758b' }, data: [{ yAxis: r.wallThicknessMm }] } }]
  }
})
const probabilityOption = computed(() => {
  const r = report.value
  if (!r || !r.annual.length) return {}
  return {
    animation: false, color: ['#2a78d6', '#eb6834'], tooltip: { ...tooltip, valueFormatter: (v: number) => percent(v / 100) }, grid: { left: 64, right: 110, top: 35, bottom: 48 },
    xAxis: { type: 'category', data: r.annual.map(p => String(p.year)), ...axis, splitLine: { show: false } },
    yAxis: { type: 'value', min: 0, max: Math.max(...r.annual.map(p => Math.max(p.probability, p.monteCarloProbability))) > 0.1 ? 100 : undefined, name: '累计穿透概率 / %', ...axis, axisLabel: { color: '#75849a', formatter: (v: number) => v === 0 ? '0' : Math.abs(v) < 0.0001 ? v.toExponential(1) : String(Number(v.toPrecision(3))) } },
    series: [
      { name: 'Gamma解析值', type: 'line', symbol: 'circle', symbolSize: 8, lineStyle: { width: 2 }, endLabel: { show: true, formatter: '解析值', color: '#52647d', offset: [0, -12] }, data: r.annual.map(p => p.probability * 100) },
      { name: '蒙特卡洛', type: 'line', symbol: 'diamond', symbolSize: 10, lineStyle: { width: 2, type: 'dashed' }, endLabel: { show: true, formatter: '蒙特卡洛', color: '#52647d', offset: [0, 12] }, data: r.annual.map(p => p.monteCarloProbability * 100) }
    ]
  }
})
async function calculate() {
  if (!segmentId.value || loading.value) return
  error.value = ''
  if (theta.value == null || !Number.isFinite(theta.value) || theta.value < 0.001 || theta.value > 5) {
    report.value = null
    error.value = '请输入0.001至5之间的Gamma尺度。'
    return
  }
  if (seed.value == null || !Number.isInteger(seed.value) || seed.value < 0 || seed.value > 2147483647) {
    report.value = null
    error.value = '请输入0至2147483647之间的整数随机种子。'
    return
  }
  const request = { segmentId: segmentId.value, theta: theta.value, sampleCount: sampleCount.value, seed: seed.value }
  loading.value = true
  report.value = null
  try {
    report.value = await riskApi.gammaPredict(request)
    await router.replace({ query: { ...route.query, segmentId: request.segmentId } })
  } catch { report.value = null; error.value = '计算未完成，请检查模型输入与后端服务。' } finally { loading.value = false }
}
onMounted(async () => {
  try {
    const segments = await corrosionApi.listSegments()
    const names = ['峰汝线A2段', '万卧线B段', '万卧线C段']
    options.value = segments.filter(s => names.includes(s.segmentName)).sort((a, b) => names.indexOf(a.segmentName) - names.indexOf(b.segmentName)).map(s => ({ value: s.segmentId, label: s.segmentName }))
    segmentId.value = options.value.find(s => s.value === Number(route.query.segmentId))?.value ?? options.value[0]?.value
    await calculate()
  } catch { error.value = '管段信息加载失败，请检查腐蚀数据服务。' }
})
</script>

<style scoped>
.risk-header { display: flex; align-items: center; justify-content: space-between; padding: 18px 24px; background: #fff; }
h2 { font-size: 20px; margin: 0; color: #20334d; }
.controls { background: #fff; padding: 20px 24px 4px; }
:deep(.ant-form-item) { margin-bottom: 16px; }
.result-heading { display: flex; flex-wrap: wrap; align-items: baseline; gap: 16px; padding: 20px 24px 0; background: #fff; color: #20334d; font-weight: 600; }
.result-heading span { color: #75849a; font-size: 12px; font-weight: 400; }
.anchor-strip { display: grid; grid-template-columns: repeat(4, 1fr); background: #fff; padding: 24px; margin-bottom: 20px; gap: 24px; }
.anchor-strip span { display: block; font-size: 12px; color: #75849a; margin-bottom: 8px; }
.anchor-strip strong { color: #20334d; font-size: 20px; font-weight: 500; }
small { font-size: 12px; color: #75849a; }
.analysis-panel { background: #fff; border: 1px solid #e2e9f2; border-radius: 8px; margin-bottom: 20px; overflow: hidden; }
.analysis-panel > header { display: flex; align-items: center; justify-content: space-between; padding: 22px 28px; gap: 16px; }
h3 { margin: 0; font-size: 17px; color: #20334d; font-weight: 600; }
.analysis-panel > header > span { color: #75849a; font-size: 12px; }
.chart { width: 100%; height: 370px; }
.legend { display: flex; gap: 18px; font-size: 12px; color: #66758b; }
.legend span { display: inline-flex; align-items: center; gap: 7px; }
.legend i { width: 8px; height: 8px; border-radius: 50%; }
.blue { background: #2a78d6; }.orange { background: #eb6834; }
.table-wrap { padding: 0 24px 24px; }
.details { padding: 4px 24px; }
.model-warning { margin-bottom: 16px; }
:deep(.ant-table) { font-size: 12px; font-variant-numeric: tabular-nums; }
@media(max-width: 900px) { .anchor-strip { grid-template-columns: repeat(2, 1fr); } .analysis-panel > header { flex-wrap: wrap; } }
</style>
