<template>
  <div class="page-container corrosion-page">
    <div class="page-header">
      <div><h2>内腐蚀趋势预测</h2></div>
      <a-upload :show-upload-list="false" accept=".xlsx,.xls" :before-upload="onImport">
        <a-button type="primary" :loading="importing"><UploadOutlined /> 导入内腐蚀数据</a-button>
      </a-upload>
    </div>
    <a-alert v-if="error" type="error" show-icon :message="error" />
    <PageCard title="预测管段基础信息">
      <a-table :data-source="forecastSegments" :columns="baseColumns" :loading="loading" row-key="segmentId" :pagination="false" :scroll="{ x: 900 }" size="middle">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'map'"><a @click="router.push({ path: '/gis', query: { segmentId: record.segmentId } })">地图定位</a></template>
        </template>
      </a-table>
    </PageCard>
    <div class="forecast-toolbar">
      <div><b>预测管段</b><a-select v-model:value="selectedId" :options="reportOptions" class="segment-select" @change="onSelect" /></div>
    </div>
    <a-spin :spinning="loading">
      <template v-if="currentReport">
        <div class="model-summary">
          <div><span>当前管段</span><strong>{{ currentReport.segmentName }}</strong></div>
          <div><span>投运日期</span><strong>{{ currentReport.commissionDate }}</strong></div>
        </div>
        <PowerLawCharts :key="currentReport.segmentId" :report="currentReport" />
        <PageCard title="原始内检记录">
          <InspectionTable :records="inspections" :loading="inspectionsLoading" />
        </PageCard>
      </template>
      <a-empty v-else-if="!loading" description="暂无预测数据" />
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { corrosionApi } from '@/api/corrosion'
import type { CorrosionSegmentVO, InspectionVO, PowerLawReport } from '@/types/corrosion'
import PageCard from '@/components/PageCard.vue'
import PowerLawCharts from './components/PowerLawCharts.vue'
import InspectionTable from './components/InspectionTable.vue'

const route = useRoute()
const router = useRouter()
const segments = ref<CorrosionSegmentVO[]>([])
const reports = ref<PowerLawReport[]>([])
const inspections = ref<InspectionVO[]>([])
const selectedId = ref<number>()
const loading = ref(false)
const importing = ref(false)
const inspectionsLoading = ref(false)
const error = ref('')
const forecastNames = ['峰汝线A2段', '万卧线B段', '万卧线C段']
const forecastSegments = computed(() => segments.value.filter(s => forecastNames.includes(s.segmentName)))
const currentReport = computed(() => reports.value.find(r => r.segmentId === selectedId.value))
const reportOptions = computed(() => reports.value.map(r => ({ label: r.segmentName, value: r.segmentId })))
const baseColumns = [
  { title: '管段', dataIndex: 'segmentName' },
  { title: '气矿', dataIndex: 'mine' },
  { title: '作业区', dataIndex: 'workArea' },
  { title: '投运日期', dataIndex: 'commissionDate' },
  { title: '壁厚（mm）', dataIndex: 'wallThicknessMm' },
  { title: '内检记录数', dataIndex: 'inspectionCount' },
  { title: '位置', key: 'map' }
]
async function loadInspections() {
  if (!selectedId.value) return
  const id = selectedId.value
  inspectionsLoading.value = true
  try {
    const result = await corrosionApi.listInspections(id)
    if (selectedId.value === id) inspections.value = result
  } catch {
    if (selectedId.value === id) inspections.value = []
  } finally {
    if (selectedId.value === id) inspectionsLoading.value = false
  }
}
function onSelect() {
  router.replace({ query: { ...route.query, segmentId: selectedId.value } })
  inspections.value = []
  void loadInspections()
}
async function load() {
  loading.value = true
  error.value = ''
  try {
    const [allSegments, reportResult] = await Promise.allSettled([corrosionApi.listSegments(), corrosionApi.getPowerLawReport()])
    if (allSegments.status === 'fulfilled') segments.value = allSegments.value
    if (reportResult.status === 'rejected') throw reportResult.reason
    const result = reportResult.value
    reports.value = result.sort((a, b) => forecastNames.indexOf(a.segmentName) - forecastNames.indexOf(b.segmentName))
    const requested = selectedId.value ?? Number(route.query.segmentId)
    selectedId.value = result.find(r => r.segmentId === requested)?.segmentId ?? result[0]?.segmentId
    await loadInspections()
  } catch {
    error.value = '腐蚀数据加载失败，请检查服务后重试。'
  } finally {
    loading.value = false
  }
}
async function onImport(file: File) {
  importing.value = true
  try {
    const result = await corrosionApi.importInspections(file)
    message.success(`导入完成：${result.imported ?? 0} 条记录，${result.segmentCount ?? 0} 个管段`)
    await load()
  } finally {
    importing.value = false
  }
  return false
}
onMounted(load)
</script>

<style scoped>
.page-header, .forecast-toolbar { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 16px; background: white; padding: 16px 20px; border-radius: 2px; }
h2 { margin: 0 0 4px; font-size: 20px; }
.forecast-toolbar > div { display: flex; align-items: center; gap: 16px; }
.forecast-toolbar > span { color: #666; }
.segment-select { width: 230px; }
.model-summary { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-bottom: 16px; padding: 20px; background: white; border-top: 2px solid #2470ff; }
.model-summary span { display: block; color: #666; font-size: 12px; margin-bottom: 8px; }
.model-summary strong { font-size: 18px; font-weight: 600; }
:deep(.report-charts) { margin-bottom: 16px; }
</style>
