<template>
  <!-- 微件模式：去除门户框架，供梦想云统一门户 iframe 嵌入 -->
  <div class="widget-layout">
    <router-view />
  </div>
</template>

<style scoped lang="less">
@import '@/styles/variables.less';

.widget-layout {
  height: 100vh;
  overflow: auto;
  background: @bg;
}
</style>
