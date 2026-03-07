import request from '@/api/request'
import type { ApiResponse } from '@/types'

/**
 * 店铺关注相关API
 */

// 关注店铺
export function followShop(shopId: number) {
  return request.post<ApiResponse<any>>(`/shop-follow/follow/${shopId}`)
}

// 取消关注店铺
export function unfollowShop(shopId: number) {
  return request.post<ApiResponse<any>>(`/shop-follow/unfollow/${shopId}`)
}

// 关注/取消关注店铺（兼容旧调用，默认关注）
export function toggleFollowShop(shopId: number) {
  return request.post<ApiResponse<any>>(`/shop-follow/follow/${shopId}`)
}

// 检查是否关注店铺
export function checkFollowStatus(shopId: number) {
  return request.get<ApiResponse<any>>(`/shop-follow/check/${shopId}`)
}

// 获取我关注的店铺列表
export function getMyFollowShops() {
  return request.get<ApiResponse<any>>('/shop-follow/my')
}

// 获取店铺详情
export function getShopDetail(shopId: number) {
  return request.get<ApiResponse<any>>(`/consumer/shop/${shopId}`)
}

// 获取店铺商品列表
export function getShopProducts(shopId: number, params: { pageNum: number; pageSize: number }) {
  return request.get<ApiResponse<any>>(`/consumer/shop/${shopId}/products`, { params })
}
