import axios, { type AxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'
import type { ApiResult } from '@/types/common'
import { clearToken, getToken, isAuthEnabled, redirectToLogin } from '@/utils/auth'

/** 业务异常 */
export class BizError extends Error {
  code: string
  constructor(code: string, msg: string) {
    super(msg)
    this.code = code
  }
}

const instance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE || '/api',
  timeout: 30000
})

instance.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

instance.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      handleUnauthorized()
    } else {
      // 不向页面暴露后台调试信息
      const msg = error?.response?.data?.msg || (status ? `服务异常（${status}）` : '网络连接失败，请稍后重试')
      message.error(msg)
    }
    return Promise.reject(error)
  }
)

function handleUnauthorized(): void {
  message.error('登录已失效，请重新登录')
  clearToken()
  if (isAuthEnabled()) redirectToLogin()
}

interface RequestOptions {
  /** 失败时不弹出提示，由调用方自行处理 */
  silent?: boolean
}

async function request<T>(config: AxiosRequestConfig, options: RequestOptions = {}): Promise<T> {
  const res = await instance.request<ApiResult<T>>(config)
  const body = res.data
  if (body.code === 'success') return body.data
  if (body.code === 'unauthorized') {
    handleUnauthorized()
  } else if (!options.silent) {
    message.error(body.msg || '操作失败')
  }
  throw new BizError(body.code, body.msg)
}

/** 仅开放 GET / POST（梦想云安全规范） */
export const http = {
  get<T>(url: string, params?: Record<string, unknown>, options?: RequestOptions): Promise<T> {
    return request<T>({ method: 'GET', url, params }, options)
  },
  post<T>(url: string, data?: unknown, options?: RequestOptions): Promise<T> {
    return request<T>({ method: 'POST', url, data }, options)
  },
  upload<T>(url: string, file: File, options?: RequestOptions): Promise<T> {
    const form = new FormData()
    form.append('file', file)
    return request<T>({ method: 'POST', url, data: form, timeout: 120000 }, options)
  }
}
