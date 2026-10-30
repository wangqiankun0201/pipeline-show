<template>
  <div>
    <a-alert
      type="info"
      show-icon
      class="exp-alert"
      message="表观指数 n_app = ln(D₂/D₁) / ln(T₂/T₁)"
      description="仅基于相同源管段ID的两期“最大深度”记录计算，未证明为同一缺陷，不得据此宣称已完成可靠的非线性拟合。"
    />
    <a-table
      :data-source="records"
      :columns="columns"
      :loading="loading"
      :pagination="false"
      :row-class-name="zebraRowClass"
      row-key="sourceSegmentId"
      size="middle"
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'depth'">{{ record.d1Pct }}% → {{ record.d2Pct }}%</template>
        <template v-else-if="column.key === 'period'">{{ record.date1 }} / {{ record.date2 }}</template>
        <template v-else-if="column.key === 't'">{{ fmtNum(record.t1Years, 2) }} / {{ fmtNum(record.t2Years, 2) }}</template>
        <template v-else-if="column.key === 'nApp'">
          <b :class="record.nApp >= 0 ? 'n-pos' : 'n-neg'">{{ record.nApp >= 0 ? '+' : '' }}{{ fmtNum(record.nApp, 4) }}</b>
        </template>
        <template v-else-if="column.key === 'direction'">
          <a-tag :color="record.direction === 'INCREASE' ? 'orange' : 'blue'">
            <RiseOutlined v-if="record.direction === 'INCREASE'" />
            <FallOutlined v-else />
            {{ record.directionName }}
          </a-tag>
        </template>
        <template v-else-if="column.key === 'judgement'">
          <a-tag :color="record.direction === 'INCREASE' ? 'default' : 'red'">{{ record.judgement }}</a-tag>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
import type { TableColumnType } from 'ant-design-vue'
import { FallOutlined, RiseOutlined } from '@ant-design/icons-vue'
import type { ApparentExponentVO } from '@/types/corrosion'
import { fmtNum } from '@/utils/format'
import { zebraRowClass } from '@/utils/table'

defineProps<{ records: ApparentExponentVO[]; loading?: boolean }>()

const columns: TableColumnType[] = [
  { title: '源ID', dataIndex: 'sourceSegmentId', width: 80, align: 'center' },
  { title: '管段名称', dataIndex: 'segmentName', width: 160 },
  { title: '两期内检日期', key: 'period', width: 200, align: 'center' },
  { title: '深度变化', key: 'depth', width: 120, align: 'center' },
  { title: 'T₁ / T₂（年）', key: 't', width: 140, align: 'center' },
  { title: '表观 n', key: 'nApp', width: 110, align: 'right' },
  { title: '方向', key: 'direction', width: 100, align: 'center' },
  { title: '数据判断', key: 'judgement', width: 140, align: 'center' }
]
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.exp-alert {
  margin-bottom: 12px;
}

.n-pos {
  color: @warning;
}

.n-neg {
  color: @primary;
}
</style>
