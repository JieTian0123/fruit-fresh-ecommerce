import request from './request'
import type { ApiResponse, HomeActivity } from '@/types'

export function getHomeActivities() {
  return request.get<ApiResponse<HomeActivity[]>>('/home/activities', { silentError: true })
}
