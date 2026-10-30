<template>
  <a-table
    :data-source="records"
    :columns="columns"
    :loading="loading"
    :pagination="{ pageSize: 10, size: 'small', hideOnSinglePage: true }"
    :row-class-name="zebraRowClass"
    row-key="assessmentId"
    size="middle"
  >
    <template #bodyCell="{ column, record }">
      <template v-if="column.key === 'status'">
        <a-tag :color="record.status === 'CALCULATED' ? 'green' : record.status === 'FAILED' ? 'red' : 'orange'">
          {{ record.statusName }}
        </a-tag>
      </template>
      <template v-else-if="column.key === 'pof'">{{ record.pofLevel ?? '—' }}</template>
      <template v-else-if="column.key === 'cof'">{{ record.cofLevel ?? '—' }}</template>
      <template v-else-if="column.key === 'riskLevel'">
        <span :style="{ color: levelColor(record.riskLevel), fontWeight: 700 }">{{ record.riskLevelName || '—' }}</span>
      </template>
      <template v-else-if="column.key === 'inputs'">
        <a-popover title="输入快照" placement="left">
          <template #content>
            <div class="snapshot">
              <div v-for="(v, k) in record.inputs" :key="k" class="snapshot-row">
                <span class="snapshot-key">{{ factorName(String(k)) }}</span>
                <span>{{ v ?? '—' }}</span>
              </div>
            </div>
          </template>
          <a>查看（{{ Object.keys(record.inputs || {}).length }} 项）</a>
        </a-popover>
      </template>
    </template>
    <template #emptyText>
      <a-empty description="暂无评价记录" />
    </template>
  </a-table>
</template>

<script setup lang="ts">
import type { TableColumnType } from 'ant-design-vue'
import type { AssessmentVO, RiskFactorGroupVO, RiskLevelDef } from '@/types/risk'
import { zebraRowClass } from '@/utils/table'

const props = defineProps<{
  records: AssessmentVO[]
  loading?: boolean
  riskLevels: RiskLevelDef[]
  groups: RiskFactorGroupVO[]
}>()

const columns: TableColumnType[] = [
  { title: '评价时间', dataIndex: 'assessTime', width: 180, align: 'center' },
  { title: '管段', dataIndex: 'segmentName', width: 160 },
  { title: '状态', key: 'status', width: 120, align: 'center' },
  { title: 'PoF 等级', key: 'pof', width: 100, align: 'center' },
  { title: 'CoF 等级', key: 'cof', width: 100, align: 'center' },
  { title: '风险等级', key: 'riskLevel', width: 120, align: 'center' },
  { title: '说明', dataIndex: 'message', ellipsis: true },
  { title: '输入快照', key: 'inputs', width: 140, align: 'center' }
]

function levelColor(code: string | null): string {
  return props.riskLevels.find((l) => l.code === code)?.color || '#999'
}

function factorName(code: string): string {
  for (const g of props.groups) {
    const f = g.factors.find((x) => x.code === code)
    if (f) return f.unit ? `${f.name}(${f.unit})` : f.name
  }
  return code
}
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.snapshot {
  max-height: 320px;
  overflow-y: auto;
  min-width: 240px;
  font-size: 12px;
}

.snapshot-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  line-height: 24px;
  border-bottom: 1px dashed @border;
}

.snapshot-key {
  color: @text-secondary;
}
</style>
