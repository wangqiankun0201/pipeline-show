/** 统一返回结构 */
export interface ApiResult<T = unknown> {
  code: 'success' | 'fail' | 'unauthorized' | string
  msg: string
  data: T
}

/** 下拉选项 */
export interface OptionItem {
  label: string
  value: string | number
}
