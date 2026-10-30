/** 数值格式化，空值显示占位符 */
export function fmtNum(value: number | null | undefined, digits = 2, placeholder = '—'): string {
  if (value === null || value === undefined || Number.isNaN(value)) return placeholder
  return Number(value).toFixed(digits)
}

/** 比值（0~1）转百分比 */
export function fmtRatio(value: number | null | undefined, digits = 1, placeholder = '—'): string {
  if (value === null || value === undefined || Number.isNaN(value)) return placeholder
  return (Number(value) * 100).toFixed(digits) + '%'
}

export function fmtText(value: string | number | null | undefined, placeholder = '—'): string {
  if (value === null || value === undefined || value === '') return placeholder
  return String(value)
}

/** 经纬度格式化 */
export function fmtCoord(lon: number | null | undefined, lat: number | null | undefined): string {
  if (lon === null || lon === undefined || lat === null || lat === undefined) return '—'
  return `${lon.toFixed(5)}°E, ${lat.toFixed(5)}°N`
}
