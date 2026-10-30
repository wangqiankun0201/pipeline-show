<template>
  <section class="page-card" :class="{ 'no-padding': noPadding }">
    <header v-if="title || $slots.extra || $slots.title" class="page-card-header">
      <div class="page-card-title">
        <span class="title-bar" />
        <slot name="title">{{ title }}</slot>
        <span v-if="subTitle" class="page-card-sub">{{ subTitle }}</span>
      </div>
      <div class="page-card-extra"><slot name="extra" /></div>
    </header>
    <div class="page-card-body">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{ title?: string; subTitle?: string; noPadding?: boolean }>()
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.page-card {
  background: #fff;
  border-radius: 2px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.page-card-header {
  height: 54px;
  flex: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  border-bottom: 1px solid @border;
}

.page-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 700;
  color: @text-main;
}

.title-bar {
  width: 4px;
  height: 16px;
  background: @primary;
  border-radius: 1px;
}

.page-card-sub {
  font-size: 12px;
  font-weight: 400;
  color: @text-secondary;
}

.page-card-extra {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-card-body {
  padding: 16px 20px;
  flex: 1;
  min-height: 0;
}

.no-padding .page-card-body {
  padding: 0;
}
</style>
