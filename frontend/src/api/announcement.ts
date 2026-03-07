import request from '@/api/request'
import type { ApiResponse } from '@/types'

/**
 * 公告相关API
 */

// 获取公告列表
export function getAnnouncementList(params: {
  pageNum: number
  pageSize: number
  type?: number
}) {
  return request.get<ApiResponse<any>>('/announcement/list', { params })
}

// 获取公告详情
export function getAnnouncementDetail(id: number) {
  return request.get<ApiResponse<any>>(`/announcement/${id}`)
}
