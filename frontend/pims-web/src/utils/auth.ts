/** 梦想云令牌存储（门户以 ?token= 方式携带进入） */
const TOKEN_KEY = 'pims_token'

export function getToken(): string | null {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  sessionStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  sessionStorage.removeItem(TOKEN_KEY)
}

export function isAuthEnabled(): boolean {
  return import.meta.env.VITE_AUTH_ENABLED === 'true'
}

/** 跳转统一门户登录 */
export function redirectToLogin(): void {
  const loginUrl = import.meta.env.VITE_PORTAL_LOGIN_URL
  if (loginUrl) {
    window.location.href = loginUrl
  }
}
