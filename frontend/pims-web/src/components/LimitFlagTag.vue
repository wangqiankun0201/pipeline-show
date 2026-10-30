<template>
  <a-tag v-if="flag" :color="meta.color" class="limit-tag">{{ name || meta.name }}</a-tag>
  <span v-else class="text-secondary">—</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { LimitFlag } from '@/types/corrosion'
import { colors } from '@/styles/theme'

const props = defineProps<{ flag?: LimitFlag | null; name?: string | null }>()

const META: Record<LimitFlag, { color: string; name: string }> = {
  NORMAL: { color: colors.success, name: '正常' },
  ATTENTION: { color: colors.warning, name: '接近壁厚' },
  MODEL_OUT_OF_RANGE: { color: colors.error, name: '模型越界' }
}

const meta = computed(() => (props.flag ? META[props.flag] : META.NORMAL) ?? { color: 'default', name: props.flag })
</script>

<style scoped>
.limit-tag {
  margin-inline-end: 0;
}
</style>
