<template>
  <div class="stat-card" :class="`tone-${tone}`">
    <div class="stat-icon">
      <slot name="icon" />
    </div>
    <div class="stat-main">
      <div class="stat-label">{{ label }}</div>
      <div class="stat-value">
        <span class="num-font">{{ value }}</span>
        <span v-if="unit" class="stat-unit">{{ unit }}</span>
      </div>
      <div v-if="hint || $slots.hint" class="stat-hint">
        <slot name="hint">{{ hint }}</slot>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    value: string | number
    unit?: string
    hint?: string
    tone?: 'primary' | 'success' | 'warning' | 'error' | 'cyan'
  }>(),
  { unit: '', hint: '', tone: 'primary' }
)
</script>

<style scoped lang="less">
@import '@/styles/variables.less';

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 18px 20px;
  border-radius: 2px;
  background: linear-gradient(90deg, #e5effe 0%, #f3f7ff 60%, #f6faff 100%);
  border: 1px solid #c3d7ff;
  min-width: 0;
}

.stat-icon {
  width: 48px;
  height: 48px;
  flex: none;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  background: @primary;
}

.stat-main {
  min-width: 0;
}

.stat-label {
  font-size: 14px;
  color: @text;
}

.stat-value {
  font-size: 32px;
  line-height: 40px;
  color: @primary;
  white-space: nowrap;
}

.stat-unit {
  font-size: 14px;
  color: @text-secondary;
  margin-left: 4px;
}

.stat-hint {
  font-size: 12px;
  color: @text-secondary;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tone-success {
  background: linear-gradient(90deg, #ecfbed 0%, #f4fdf5 100%);
  border-color: #b7eb8f;
  .stat-icon {
    background: @success;
  }
  .stat-value {
    color: @success;
  }
}

.tone-warning {
  background: linear-gradient(90deg, #fff7eb 0%, #fffaf2 100%);
  border-color: #ffebcc;
  .stat-icon {
    background: @warning;
  }
  .stat-value {
    color: @warning;
  }
}

.tone-error {
  background: linear-gradient(90deg, #fff3f4 0%, #fff8f8 100%);
  border-color: #ffa39e;
  .stat-icon {
    background: @error;
  }
  .stat-value {
    color: @error;
  }
}

.tone-cyan {
  background: linear-gradient(90deg, #e1f5ff 0%, #effafe 60%, #f4fdff 100%);
  border-color: #91d5ff;
  .stat-icon {
    background: #00a2fa;
  }
  .stat-value {
    color: #00a2fa;
  }
}
</style>
