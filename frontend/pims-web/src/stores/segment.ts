import { defineStore } from 'pinia'

/** 跨页面共享的当前选中管段 */
export const useSegmentStore = defineStore('segment', {
  state: () => ({ currentSegmentId: null as number | null }),
  actions: {
    select(segmentId: number | null) {
      this.currentSegmentId = segmentId
    }
  }
})
