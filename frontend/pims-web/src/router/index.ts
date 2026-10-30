import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { getToken, isAuthEnabled, redirectToLogin, setToken } from '@/utils/auth'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  // 门户以 ?token= 携带梦想云令牌进入，保存后从地址栏移除
  const urlToken = to.query.token
  if (typeof urlToken === 'string' && urlToken) {
    setToken(urlToken)
    const query = { ...to.query }
    delete query.token
    return { path: to.path, query, hash: to.hash, replace: true }
  }
  if (isAuthEnabled() && !getToken()) {
    redirectToLogin()
    return false
  }
  return true
})

router.afterEach((to) => {
  const appTitle = import.meta.env.VITE_APP_TITLE || '管道完整性管理平台'
  const title = (to.meta?.title as string) || ''
  document.title = title ? `${title} - ${appTitle}` : appTitle
})

export default router
