import request from '@/api/request'
import type { ApiResponse } from '@/types'
import type { VipPlan, VipOrder, VipStatus } from '@/types'

/**
 * VIP会员相关API
 */

// 获取VIP状态
export function getVipStatus() {
  return request.get<ApiResponse<VipStatus>>('/consumer/vip/status')
}

// 获取可用VIP套餐列表
export function getVipPlans() {
  return request.get<ApiResponse<VipPlan[]>>('/consumer/vip/plans')
}

// 购买VIP
export function purchaseVip(planId: number) {
  return request.post<ApiResponse<VipOrder>>(`/consumer/vip/purchase/${planId}`)
}

/**
 * 管理员VIP管理API
 */

// 获取VIP套餐列表（管理员）
export function getAdminVipPlans(pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<any>>('/admin/vip/plans', {
    params: { pageNum, pageSize }
  })
}

// 添加VIP套餐
export function addVipPlan(plan: Partial<VipPlan>) {
  return request.post<ApiResponse<void>>('/admin/vip/plan', plan)
}

// 修改VIP套餐
export function updateVipPlan(plan: Partial<VipPlan>) {
  return request.put<ApiResponse<void>>('/admin/vip/plan', plan)
}

// 删除VIP套餐
export function deleteVipPlan(id: number) {
  return request.delete<ApiResponse<void>>(`/admin/vip/plan/${id}`)
}

// 更新套餐状态
export function updateVipPlanStatus(id: number, status: number) {
  return request.put<ApiResponse<void>>(`/admin/vip/plan/${id}/status`, null, {
    params: { status }
  })
}

// 获取VIP用户列表
export function getVipUsers(pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<any>>('/admin/vip/users', {
    params: { pageNum, pageSize }
  })
}

// 获取VIP订单列表
export function getVipOrders(pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<any>>('/admin/vip/orders', {
    params: { pageNum, pageSize }
  })
}
