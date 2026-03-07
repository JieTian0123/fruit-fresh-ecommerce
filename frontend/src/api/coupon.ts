import request from '@/api/request'
import type { ApiResponse } from '@/types'

/**
 * 优惠券相关API
 */

// 获取可领取优惠券列表
export function getAvailableCoupons() {
  return request.get<ApiResponse<any>>('/coupon/available')
}

// 领取优惠券
export function receiveCoupon(id: number) {
  return request.post<ApiResponse<any>>(`/coupon/receive/${id}`)
}

// 获取我的优惠券
export function getMyCoupons(status?: number) {
  return request.get<ApiResponse<any>>('/coupon/my', { 
    params: status !== undefined ? { status } : {}
  })
}

// 获取可用优惠券（结算页）
export function getUsableCoupons(amount: number) {
  return request.get<ApiResponse<any>>('/coupon/usable', { params: { amount } })
}

// 获取可积分兑换的优惠券列表
export function getExchangeableCoupons(pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<any>>('/coupon/exchangeable', {
    params: { pageNum, pageSize }
  })
}

// 积分兑换优惠券
export function exchangeCoupon(couponId: number) {
  return request.post<ApiResponse<any>>(`/coupon/exchange/${couponId}`)
}
