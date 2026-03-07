import request from './request'
import type { ApiResponse, PageResult, Order, CreateOrderDTO, OrderQuery } from '@/types'

// 创建订单
export function createOrder(data: CreateOrderDTO) {
  return request.post<ApiResponse<Order>>('/consumer/order/create', data)
}

// 获取订单列表
export function getOrderList(params: OrderQuery) {
  return request.get<ApiResponse<PageResult<Order>>>('/consumer/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(orderNo: string) {
  return request.get<ApiResponse<Order>>(`/consumer/order/detail/${orderNo}`)
}

// 支付订单
export function payOrder(orderNo: string) {
  return request.put<ApiResponse>(`/consumer/order/pay/${orderNo}`)
}

// 确认收货
export function confirmReceive(orderNo: string) {
  return request.put<ApiResponse>(`/consumer/order/confirm/${orderNo}`)
}

// 取消订单
export function cancelOrder(orderNo: string) {
  return request.put<ApiResponse>(`/consumer/order/cancel/${orderNo}`)
}

// 申请退款
export function refundOrder(orderNo: string) {
  return request.put<ApiResponse>(`/consumer/order/refund/${orderNo}`)
}

// 删除订单
export function deleteOrder(id: number) {
  return request.delete<ApiResponse>(`/consumer/order/${id}`)
}

// ===== 商家订单接口 =====

// 商家发货
export function shipOrder(data: { orderNo: string; deliveryCompany: string; deliveryNo: string }) {
  return request.post<ApiResponse>('/merchant/order/deliver', data)
}
