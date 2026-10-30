import { http } from './http'

export interface CurrentUserVO {
  userId: string
  loginName: string
  displayName: string
  orgId: string | null
}

/** 网关鉴权：当前用户（网关调用梦想云用户中心 currentuser） */
export const authApi = {
  getCurrentUser: () => http.get<CurrentUserVO>('/auth/currentuser', undefined, { silent: true })
}
