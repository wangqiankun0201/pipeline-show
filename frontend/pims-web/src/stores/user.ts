import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'

interface UserState {
  userId: string
  loginName: string
  displayName: string
  orgId: string | null
  loaded: boolean
}

/** 当前用户（来自网关 /auth/currentuser，网关负责调用用户中心校验令牌） */
export const useUserStore = defineStore('user', {
  state: (): UserState => ({ userId: '', loginName: '', displayName: '', orgId: null, loaded: false }),
  actions: {
    async loadCurrentUser() {
      if (this.loaded) return
      try {
        const user = await authApi.getCurrentUser()
        this.userId = user.userId
        this.loginName = user.loginName
        this.displayName = user.displayName
        this.orgId = user.orgId
        this.loaded = true
      } catch {
        // 未登录时由 http 拦截器统一处理跳转
        this.displayName = '未登录'
      }
    }
  }
})
