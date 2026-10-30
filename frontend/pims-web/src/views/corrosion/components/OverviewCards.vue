<template>
  <a-skeleton :loading="loading" active :paragraph="{ rows: 1 }">
    <div class="overview-cards">
      <StatCard label="管段数量" :value="overview?.segmentCount ?? '—'" unit="段" tone="primary" :hint="modelHint">
        <template #icon><ApartmentOutlined /></template>
      </StatCard>
      <StatCard label="内检记录" :value="overview?.inspectionCount ?? '—'" unit="条" tone="cyan" hint="内腐蚀数据.xlsx">
        <template #icon><FileSearchOutlined /></template>
      </StatCard>
      <StatCard
        label="可计算管段"
        :value="overview?.predictableCount ?? '—'"
        unit="段"
        tone="success"
        :hint="`最近内检：${overview?.latestInspectionDate || '—'}`"
      >
        <template #icon><CheckCircleOutlined /></template>
      </StatCard>
      <StatCard
        label="k 中位数（n=1.0）"
        :value="fmtNum(overview?.medianK, 4)"
        unit="mm/年"
        tone="warning"
        hint="投运至检测累计深度 / 年数"
      >
        <template #icon><RiseOutlined /></template>
      </StatCard>
      <StatCard
        label="最大腐蚀深度"
        :value="overview?.maxDepthPct ?? '—'"
        unit="% 壁厚"
        tone="error"
        hint="全部内检记录最大值"
      >
        <template #icon><WarningOutlined /></template>
      </StatCard>
    </div>
  </a-skeleton>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import {
  ApartmentOutlined,
  CheckCircleOutlined,
  FileSearchOutlined,
  RiseOutlined,
  WarningOutlined
} from '@ant-design/icons-vue'
import StatCard from '@/components/StatCard.vue'
import type { CorrosionOverviewVO } from '@/types/corrosion'
import { fmtNum } from '@/utils/format'

const props = defineProps<{ overview: CorrosionOverviewVO | null; loading?: boolean }>()

const modelHint = computed(() =>
  props.overview ? `${props.overview.modelName} ${props.overview.modelVersion}，基准 n=${props.overview.baseN}` : ''
)
</script>

<style scoped>
.overview-cards {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 16px;
}
</style>
