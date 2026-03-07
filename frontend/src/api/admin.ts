import request from './request'
import type { ApiResponse, PageResult, UserInfo, Product, Order, Banner, Coupon, Announcement, MerchantShop } from '@/types'

/**
 * 管理员API模块
 */

// 用户管理
export function getUserList(params: {
  userType?: number
  status?: number
  pageNum: number
  pageSize: number
}) {
  return request.get<ApiResponse<PageResult<UserInfo>>>('/admin/user/list', { params })
}

export function getUserDetail(id: number) {
  return request.get<ApiResponse<UserInfo>>(`/admin/user/detail/${id}`)
}

export function approveUser(id: number, status: number) {
  return request.put<ApiResponse<void>>(`/admin/user/approve/${id}`, null, { params: { status } })
}

export function disableUser(id: number) {
  return request.put<ApiResponse<void>>(`/admin/user/disable/${id}`)
}

export function enableUser(id: number) {
  return request.put<ApiResponse<void>>(`/admin/user/enable/${id}`)
}

// 商品管理
export function getProductListForAdmin(params: {
  status?: number
  keyword?: string
  categoryId?: number
  pageNum: number
  pageSize: number
}) {
  return request.get<ApiResponse<PageResult<Product>>>('/admin/product/list', { params })
}

export function getProductDetail(id: number) {
  return request.get<ApiResponse<Product>>(`/admin/product/detail/${id}`)
}

export function approveProduct(id: number, status: number) {
  return request.put<ApiResponse<void>>(`/admin/product/approve/${id}`, null, { params: { status } })
}

export function deleteProduct(id: number) {
  return request.delete<ApiResponse<void>>(`/admin/product/delete/${id}`)
}

// 订单管理
export function getOrderListForAdmin(params: {
  status?: number
  orderNo?: string
  pageNum: number
  pageSize: number
}) {
  return request.get<ApiResponse<PageResult<Order>>>('/admin/order/list', { params })
}

export function getOrderDetailForAdmin(orderNo: string) {
  return request.get<ApiResponse<any>>(`/admin/order/detail/${orderNo}`)
}

// Banner管理
export function getBannerList(params: { pageNum: number; pageSize: number }) {
  return request.get<ApiResponse<PageResult<Banner>>>('/admin/banner/list', { params })
}

export function getBannerDetail(id: number) {
  return request.get<ApiResponse<Banner>>(`/admin/banner/detail/${id}`)
}

export function addBanner(data: Partial<Banner>) {
  return request.post<ApiResponse<void>>('/admin/banner/add', data)
}

export function updateBanner(id: number, data: Partial<Banner>) {
  return request.put<ApiResponse<void>>(`/admin/banner/update/${id}`, data)
}

export function deleteBanner(id: number) {
  return request.delete<ApiResponse<void>>(`/admin/banner/delete/${id}`)
}

export function updateBannerStatus(id: number, status: number) {
  return request.put<ApiResponse<void>>(`/admin/banner/status/${id}`, null, { params: { status } })
}

// 优惠券管理
export function getCouponList(params: { status?: number; keyword?: string; pageNum: number; pageSize: number }) {
  return request.get<ApiResponse<PageResult<Coupon>>>('/admin/coupon/list', { params })
}

export function getCouponDetail(id: number) {
  return request.get<ApiResponse<Coupon>>(`/admin/coupon/detail/${id}`)
}

export function addCoupon(data: Partial<Coupon>) {
  return request.post<ApiResponse<void>>('/admin/coupon/add', data)
}

export function updateCoupon(data: Partial<Coupon>) {
  return request.put<ApiResponse<void>>('/admin/coupon/update', data)
}

export function deleteCoupon(id: number) {
  return request.delete<ApiResponse<void>>(`/admin/coupon/delete/${id}`)
}

export function updateCouponStatus(id: number, status: number) {
  return request.put<ApiResponse<void>>(`/admin/coupon/status/${id}`, null, { params: { status } })
}

// 公告管理
export function getAnnouncementList(params: { status?: number; keyword?: string; pageNum: number; pageSize: number }) {
  return request.get<ApiResponse<PageResult<Announcement>>>('/admin/announcement/list', { params })
}

export function getAnnouncementDetail(id: number) {
  return request.get<ApiResponse<Announcement>>(`/admin/announcement/detail/${id}`)
}

export function addAnnouncement(data: Partial<Announcement>) {
  return request.post<ApiResponse<void>>('/admin/announcement/add', data)
}

export function updateAnnouncement(data: Partial<Announcement>) {
  return request.put<ApiResponse<void>>('/admin/announcement/update', data)
}

export function publishAnnouncement(id: number) {
  return request.post<ApiResponse<void>>(`/admin/announcement/publish/${id}`)
}

export function unpublishAnnouncement(id: number) {
  return request.post<ApiResponse<void>>(`/admin/announcement/unpublish/${id}`)
}

export function deleteAnnouncement(id: number) {
  return request.delete<ApiResponse<void>>(`/admin/announcement/delete/${id}`)
}

// 店铺管理
export function getShopList(params: { status?: number; pageNum: number; pageSize: number }) {
  return request.get<ApiResponse<PageResult<MerchantShop>>>('/admin/shop/list', { params })
}

export function getShopDetail(id: number) {
  return request.get<ApiResponse<MerchantShop>>(`/admin/shop/detail/${id}`)
}

export function approveShop(id: number, status: number) {
  return request.put<ApiResponse<void>>(`/admin/shop/approve/${id}`, null, { params: { status } })
}

// 统计
export function getAdminOverview() {
  return request.get<ApiResponse<Record<string, number>>>('/admin/stats/overview')
}

export function getAdminGrowth() {
  return request.get<ApiResponse<Record<string, number>>>('/admin/stats/growth')
}

// 时段统计
export function getAdminPeriodStats(period: string) {
  return request.get<ApiResponse<any>>('/admin/stats/period', { params: { period } })
}
