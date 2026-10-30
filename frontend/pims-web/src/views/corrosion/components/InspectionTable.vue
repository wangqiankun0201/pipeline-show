<template>
  <a-table
    :data-source="records"
    :columns="columns"
    :loading="loading"
    :pagination="false"
    :row-class-name="zebraRowClass"
    row-key="inspectionId"
    size="middle"
    :scroll="{ x: 1200 }"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'inspectionDate'">
        <span>{{ record.inspectionDate || '—' }}</span>
        <a-tag v-if="record.isLatest" color="blue" class="latest-tag">最新</a-tag>
      </template>
      <template v-else-if="column.key === 'maxDepthPct'">
        <span v-if="record.maxDepthPct !== null" :class="depthClass(record.maxDepthPct)">{{ record.maxDepthPct }}%</span>
        <span v-else>—</span>
      </template>
      <template v-else-if="column.key === 'operatingPressure'">
        {{ record.operatingPressureText || fmtText(record.operatingPressureMpa) }}
      </template>
      <template v-else-if="column.key === 'remark'">
        <a-tag v-if="record.remark" color="default">{{ record.remark }}</a-tag>
        <span v-else>—</span>
      </template>
      <template v-else-if="column.dataIndex">
        {{ fmtText(record[column.dataIndex as keyof InspectionVO] as string | number | null) }}
      </template>
    </template>
    <template #expandedRowRender="{ record }">
      <a-descriptions :column="6" size="small" bordered class="env-desc" title="运行环境与防护">
        <a-descriptions-item label="运行温度">{{ fmtText(record.operatingTemperature) }}</a-descriptions-item>
        <a-descriptions-item label="压力系数">{{ fmtText(record.pressureFactor) }}</a-descriptions-item>
        <a-descriptions-item label="是否积液">{{ fmtText(record.liquidAccumulation) }}</a-descriptions-item>
        <a-descriptions-item label="CO₂ (mol%)">{{ fmtText(record.co2MolPct) }}</a-descriptions-item>
        <a-descriptions-item label="H₂S (mol%)">{{ fmtText(record.h2sMolPct) }}</a-descriptions-item>
        <a-descriptions-item label="O₂ (mol%)">{{ fmtText(record.o2MolPct) }}</a-descriptions-item>
        <a-descriptions-item label="含水率 (%)">{{ fmtText(record.waterCutPct) }}</a-descriptions-item>
        <a-descriptions-item label="Cl⁻ (mg/L)">{{ fmtText(record.chlorideMgL) }}</a-descriptions-item>
        <a-descriptions-item label="pH">{{ fmtText(record.ph) }}</a-descriptions-item>
        <a-descriptions-item label="缓蚀剂投加">{{ fmtText(record.inhibitorDosing) }}</a-descriptions-item>
        <a-descriptions-item label="缓蚀剂浓度">{{ fmtText(record.inhibitorConcentration) }}</a-descriptions-item>
        <a-descriptions-item label="清管周期 (天)">{{ fmtText(record.piggingCycleDays) }}</a-descriptions-item>
        <a-descriptions-item label="外径 (mm)">{{ fmtText(record.outerDiameterMm) }}</a-descriptions-item>
        <a-descriptions-item label="设计压力 (MPa)">{{ fmtText(record.designPressureMpa) }}</a-descriptions-item>
        <a-descriptions-item label="平均 ERF">{{ fmtText(record.avgErf) }}</a-descriptions-item>
        <a-descriptions-item label="最大 ERF">{{ fmtText(record.maxErf) }}</a-descriptions-item>
        <a-descriptions-item label="源管段ID">{{ fmtText(record.sourceSegmentId) }}</a-descriptions-item>
        <a-descriptions-item label="服役年限">{{ fmtNum(record.serviceYears, 2) }}</a-descriptions-item>
      </a-descriptions>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import type { TableColumnType } from 'ant-design-vue'
import type { InspectionVO } from '@/types/corrosion'
import { fmtNum, fmtText } from '@/utils/format'
import { zebraRowClass } from '@/utils/table'

defineProps<{ records: InspectionVO[]; loading?: boolean }>()

const columns: TableColumnType[] = [
  { title: '内检日期', key: 'inspectionDate', width: 150, align: 'center', fixed: 'left' },
  { title: '源管段ID', dataIndex: 'sourceSegmentId', width: 90, align: 'center' },
  { title: '管段', dataIndex: 'segmentName', width: 130 },
  { title: '壁厚(mm)', dataIndex: 'wallThicknessMm', width: 90, align: 'right' },
  { title: '材质', dataIndex: 'materialGrade', width: 80, align: 'center' },
  { title: '运行压力(MPa)', key: 'operatingPressure', width: 120, align: 'right' },
  { title: '介质', dataIndex: 'mediumType', width: 100, align: 'center' },
  { title: '缺陷数量', dataIndex: 'defectCount', width: 90, align: 'right' },
  { title: '最大深度', key: 'maxDepthPct', width: 90, align: 'right' },
  { title: '最大深度(mm)', dataIndex: 'maxDepthMm', width: 110, align: 'right' },
  { title: '平均ERF', dataIndex: 'avgErf', width: 90, align: 'right' },
  { title: '最大ERF', dataIndex: 'maxErf', width: 90, align: 'right' },
  { title: '备注', key: 'remark', width: 120, align: 'center' }
]

function depthClass(pct: number): string {
  if (pct >= 80) return 'depth-high'
  if (pct >= 40) return 'depth-mid'
  return 'depth-low'
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.latest-tag {
  margin-left: 6px;
  margin-inline-end: 0;
}

.depth-high {
  color: @error;
  font-weight: 700;
}

.depth-mid {
  color: @warning;
  font-weight: 700;
}

.depth-low {
  color: @success;
}

.env-desc {
  :deep(.ant-descriptions-title) {
    font-size: 14px;
  }
  :deep(.ant-descriptions-item-label),
  :deep(.ant-descriptions-item-content) {
    font-size: 12px;
  }
}
</style>
