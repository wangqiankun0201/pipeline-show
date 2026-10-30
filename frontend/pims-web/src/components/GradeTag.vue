<template>
  <a-tooltip :title="GRADE_DESC[grade] || ''">
    <a-tag :color="GRADE_COLOR[grade] || 'default'">{{ grade }} · {{ name || GRADE_NAME[grade] }}</a-tag>
  </a-tooltip>
</template>

<script setup lang="ts">
import type { DataGrade } from '@/types/corrosion'

defineProps<{ grade: DataGrade; name?: string | null }>()

const GRADE_COLOR: Record<string, string> = { A: 'green', B: 'blue', C: 'orange', D: 'default' }
const GRADE_NAME: Record<string, string> = { A: '缺陷匹配', B: '两期管段', C: '单期锚定', D: '缺失' }
const GRADE_DESC: Record<string, string> = {
  A: '同一缺陷多期配准，可估计增长率',
  B: '同一管段两期检测，可计算表观指数',
  C: '仅以最近一次内检锚定，按默认指数试算',
  D: '关键字段缺失，不可计算'
}
</script>
