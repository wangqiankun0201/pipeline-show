<template>
  <a-table
    :data-source="segments"
    :columns="columns"
    :loading="loading"
    :pagination="false"
    :row-class-name="rowClass"
    row-key="segmentId"
    size="middle"
    :custom-row="customRow"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'segmentName'">
        <a class="seg-link">{{ record.segmentName }}</a>
      </template>
      <template v-else-if="column.key === 'grade'">
        <GradeTag :grade="record.grade" :name="record.gradeName" />
      </template>
      <template v-else-if="column.key === 'latestMaxDepthPct'">
        {{ record.latestMaxDepthPct !== null ? `${record.latestMaxDepthPct}%` : '—' }}
      </template>
      <template v-else-if="column.key === 'd0Mm'">{{ fmtNum(record.d0Mm, 3) }}</template>
      <template v-else-if="column.key === 'tYears'">{{ fmtNum(record.tYears, 2) }}</template>
      <template v-else-if="column.key === 'kValue'">
        <b v-if="record.kValue !== null" class="k-value">{{ fmtNum(record.kValue, 4) }}</b>
        <span v-else>—</span>
      </template>
      <template v-else-if="column.key === 'note'">
        <span v-if="record.predictable" class="ok-text">可计算</span>
        <a-tooltip v-else :title="record.note">
          <a-tag color="default">{{ record.note || '不可计算' }}</a-tag>
        </a-tooltip>
      </template>
    </template>
  </a-table>
</template>

<script setup lang="ts">
import type { TableColumnType } from 'ant-design-vue'
import GradeTag from '@/components/GradeTag.vue'
import type { CorrosionSegmentVO } from '@/types/corrosion'
import { fmtNum } from '@/utils/format'

const props = defineProps<{ segments: CorrosionSegmentVO[]; loading?: boolean; selectedId: number | null }>()
const emit = defineEmits<{ (e: 'select', segmentId: number): void }>()

const columns: TableColumnType[] = [
  { title: '管段ID', dataIndex: 'segmentId', width: 80, align: 'center' },
  { title: '气矿', dataIndex: 'mine', width: 100, align: 'center' },
  { title: '管线', dataIndex: 'pipelineName', width: 100 },
  { title: '管段', key: 'segmentName', width: 130 },
  { title: '投运日期', dataIndex: 'commissionDate', width: 110, align: 'center' },
  { title: '最新内检', dataIndex: 'latestInspectionDate', width: 110, align: 'center' },
  { title: '内检次数', dataIndex: 'inspectionCount', width: 80, align: 'center' },
  { title: '壁厚(mm)', dataIndex: 'wallThicknessMm', width: 90, align: 'right' },
  { title: '最大深度', key: 'latestMaxDepthPct', width: 90, align: 'right' },
  { title: 'd₀(mm)', key: 'd0Mm', width: 90, align: 'right' },
  { title: 'T(年)', key: 'tYears', width: 80, align: 'right' },
  { title: 'k(mm/年)', key: 'kValue', width: 100, align: 'right' },
  { title: '可信等级', key: 'grade', width: 130, align: 'center' },
  { title: '计算状态', key: 'note', width: 140, align: 'center' }
]

function rowClass(record: CorrosionSegmentVO, index: number): string {
  const classes = [index % 2 === 1 ? 'zebra-even' : '']
  if (record.segmentId === props.selectedId) classes.push('row-selected')
  return classes.join(' ')
}

function customRow(record: CorrosionSegmentVO) {
  return { onClick: () => emit('select', record.segmentId), style: { cursor: 'pointer' } }
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.seg-link {
  color: @primary;
}

.k-value {
  color: @primary-deep;
}

.ok-text {
  color: @success;
}

:deep(.row-selected > td) {
  background: rgba(36, 112, 255, 0.06) !important;
}
</style>
