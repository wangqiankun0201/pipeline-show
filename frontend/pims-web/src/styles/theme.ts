import type { ThemeConfig } from 'ant-design-vue/es/config-provider/context'

/** 梦想云 UI 规范色值 */
export const colors = {
  primary: '#2470ff',
  primaryHover: '#4585FF',
  primaryDeep: '#1760E8',
  primaryLight: '#E3EBFF',
  success: '#3BC243',
  warning: '#FF9E0F',
  error: '#F35159',
  important: '#fe377e',
  textMain: '#333333',
  text: '#666666',
  textSecondary: '#999999',
  disabled: '#C1C2C1',
  border: '#E9EDF0',
  bg: '#F3F4F6'
}

/** 图表/管线配色（梦想云图表配色） */
export const chartPalette = ['#2470ff', '#00A2FA', '#1ae1e5', '#f5c02b', '#4D81E7', '#3BC243', '#fe377e', '#FF9E0F']

/** 地图管线配色：相邻管线色相差异最大化，保证在各类底图上突出可辨 */
export const pipelinePalette = ['#2470ff', '#f5c02b', '#fe377e', '#1ae1e5', '#4D81E7', '#00A2FA']

export const antdTheme: ThemeConfig = {
  token: {
    colorPrimary: colors.primary,
    colorSuccess: colors.success,
    colorWarning: colors.warning,
    colorError: colors.error,
    colorInfo: colors.primary,
    colorText: colors.textMain,
    colorTextSecondary: colors.text,
    colorBorder: colors.border,
    colorBorderSecondary: colors.border,
    borderRadius: 2,
    fontSize: 14,
    controlHeight: 36,
    controlHeightSM: 32,
    controlHeightLG: 40,
    fontFamily: "'Microsoft YaHei', '微软雅黑', Arial, sans-serif"
  }
}
