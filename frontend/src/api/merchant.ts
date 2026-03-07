import request from './request'
import type { ApiResponse, PageResult, Product, Order, MerchantShop, ShopDTO } from '@/types'

/**
 * 商家API模块
 */

// 商品管理
export function getMerchantProductList(params: {
  status?: number
  pageNum: number
  pageSize: number
}) {
  return request.get<ApiResponse<PageResult<Product>>>('/merchant/product/list', { params })
}

// 商品上架
export function onSaleProduct(id: number) {
  return request.put<ApiResponse<void>>(`/merchant/product/onSale/${id}`)
}

// 商品下架
export function offSaleProduct(id: number) {
  return request.put<ApiResponse<void>>(`/merchant/product/offSale/${id}`)
}

// 订单管理
export function getMerchantOrderList(params: {
  status?: number
  pageNum: number
  pageSize: number
}) {
  return request.get<ApiResponse<PageResult<Order>>>('/merchant/order/list', { params })
}

// 店铺管理
export function getShopInfo() {
  return request.get<ApiResponse<MerchantShop>>('/merchant/shop/info')
}

export function createShop(data: ShopDTO) {
  return request.post<ApiResponse<void>>('/merchant/shop/create', data)
}

export function updateShop(data: ShopDTO) {
  return request.put<ApiResponse<void>>('/merchant/shop/update', data)
}

// 统计
export function getMerchantOverview() {
  return request.get<ApiResponse<Record<string, number>>>('/merchant/stats/overview')
}

// 时段统计
export function getMerchantPeriodStats(period: string) {
  return request.get<ApiResponse<any>>('/merchant/stats/period', { params: { period } })
}

// ===== 溯源管理 =====

// 添加溯源记录
export function addTraceability(data: any) {
  return request.post<ApiResponse<void>>('/merchant/traceability/add', data)
}

// 删除溯源记录
export function deleteTraceability(id: number) {
  return request.delete<ApiResponse<void>>(`/merchant/traceability/${id}`)
}

// 获取商品溯源记录列表
export function getProductTraceabilityList(productId: number) {
  return request.get<ApiResponse<any[]>>(
    `/merchant/traceability/list/${productId}`
  )
}

// ===== 会员客户管理 =====

// 获取会员客户列表
export function getMemberCustomers(pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<PageResult<any>>>('/merchant/member-customers/list', {
    params: { pageNum, pageSize }
  })
}
