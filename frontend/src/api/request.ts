import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import type { ApiResponse } from '@/types'

// 登出标记：登出期间抑制401错误提示
let isLoggingOut = false

export function setLoggingOut(value: boolean) {
  isLoggingOut = value
}

// 创建 axios 实例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = token
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 返回 response.data (ApiResponse)
service.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    const res = response.data
    const silentError = (response.config as AxiosRequestConfig & { silentError?: boolean }).silentError === true
    
    // 业务状态码判断
    if (res.code !== 200) {
      // 登出期间抑制错误提示
      if (!isLoggingOut && !silentError) {
        ElMessage.error(res.message || '请求失败')
      }
      
      // 401 未登录或token过期
      if (res.code === 401) {
        if (!isLoggingOut) {
          handleUnauthorized()
        }
      }
      
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    
    // 转换 Address.isDefault: number → boolean
    if (res.data) {
      // 单个地址对象
      if (res.data.isDefault !== undefined && typeof res.data.isDefault === 'number') {
        res.data.isDefault = res.data.isDefault === 1
      }
      // 地址列表
      if (Array.isArray(res.data) && res.data.length > 0 && res.data[0].isDefault !== undefined) {
        res.data.forEach((item: any) => {
          if (typeof item.isDefault === 'number') {
            item.isDefault = item.isDefault === 1
          }
        })
      }
    }
    
    return res as any
  },
  (error) => {
    const silentError = (error.config as AxiosRequestConfig & { silentError?: boolean } | undefined)?.silentError === true
    
    if (silentError) {
      return Promise.reject(error)
    }

    console.error('Request error:', error)

    if (error.response) {
      const status = error.response.status
      switch (status) {
        case 401:
          if (!isLoggingOut) {
            ElMessage.error('登录已过期，请重新登录')
            handleUnauthorized()
          }
          break
        case 403:
          ElMessage.error('没有权限访问')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(error.response.data?.message || '请求失败')
      }
    } else if (error.message.includes('timeout')) {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

// 统一处理401未授权
function handleUnauthorized() {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  
  // 获取当前路由，如果不是公开路由则跳转登录
  const currentPath = router.currentRoute.value.path
  const publicRoutes = ['/', '/category', '/product', '/search', '/login', '/register']
  const isPublicRoute = publicRoutes.some(route => 
    currentPath === route || currentPath.startsWith(route + '/')
  )
  
  // 仅受保护路由跳转登录页
  if (!isPublicRoute) {
    router.push({ name: 'Login', query: { redirect: currentPath } })
  }
}

type SilentAxiosRequestConfig = AxiosRequestConfig & {
  silentError?: boolean
}

type RequestInstance = {
  request<T = any>(config: SilentAxiosRequestConfig): Promise<T>
  get<T = any>(url: string, config?: SilentAxiosRequestConfig): Promise<T>
  post<T = any>(url: string, data?: any, config?: SilentAxiosRequestConfig): Promise<T>
  put<T = any>(url: string, data?: any, config?: SilentAxiosRequestConfig): Promise<T>
  delete<T = any>(url: string, config?: SilentAxiosRequestConfig): Promise<T>
}

const request: RequestInstance = {
  request(config) {
    return service.request(config) as unknown as Promise<any>
  },
  get(url, config) {
    return service.get(url, config) as unknown as Promise<any>
  },
  post(url, data, config) {
    return service.post(url, data, config) as unknown as Promise<any>
  },
  put(url, data, config) {
    return service.put(url, data, config) as unknown as Promise<any>
  },
  delete(url, config) {
    return service.delete(url, config) as unknown as Promise<any>
  }
}

export default request
