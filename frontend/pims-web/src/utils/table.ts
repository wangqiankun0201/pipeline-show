/** 表格斑马纹：#F9F9F9 / #FFF（梦想云表格规范） */
export function zebraRowClass(_record: unknown, index: number): string {
  return index % 2 === 1 ? 'zebra-even' : ''
}
