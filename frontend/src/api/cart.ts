import request from './request'
import type { ApiResponse, CartItem, CartItemDTO } from '@/types'

// 获取购物车列表
export function getCartList() {
  return request.get<ApiResponse<CartItem[]>>('/consumer/cart/list')
}

// 添加商品到购物车
export function addToCart(data: CartItemDTO) {
  return request.post<ApiResponse>('/consumer/cart/add', data)
}

// 更新购物车商品数量
export function updateCartItem(id: number, quantity: number) {
  return request.put<ApiResponse>(`/consumer/cart/update/${id}`, null, { params: { quantity } })
}

// 删除购物车商品
export function deleteCartItem(id: number) {
  return request.delete<ApiResponse>(`/consumer/cart/delete/${id}`)
}

// 批量删除购物车商品
export function batchDeleteCartItems(ids: number[]) {
  return request.delete<ApiResponse>('/consumer/cart/batch', { data: { ids } })
}

// 选中/取消选中购物车商品
export function selectCartItem(id: number, selected: boolean) {
  return request.put<ApiResponse>(`/consumer/cart/select/${id}`, null, { params: { selected: selected ? 1 : 0 } })
}

// 全选/取消全选
export function selectAllCartItems(selected: boolean) {
  return request.put<ApiResponse>('/consumer/cart/selectAll', null, { params: { selected: selected ? 1 : 0 } })
}

// 获取购物车商品数量
export function getCartCount() {
  return request.get<ApiResponse<number>>('/consumer/cart/count')
}

// 清空购物车
export function clearCart() {
  return request.delete<ApiResponse>('/consumer/cart/clear')
}
