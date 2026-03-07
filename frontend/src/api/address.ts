import request from './request'
import type { ApiResponse, Address, AddressDTO } from '@/types'

// 获取地址列表
export function getAddressList() {
  return request.get<ApiResponse<Address[]>>('/consumer/address/list')
}

// 获取地址详情
export function getAddressDetail(id: number) {
  return request.get<ApiResponse<Address>>(`/consumer/address/${id}`)
}

// 添加地址
export function addAddress(data: AddressDTO) {
  // 转换 isDefault: boolean → number (0/1) 给后端
  const payload = {
    ...data,
    isDefault: data.isDefault ? 1 : 0
  }
  return request.post<ApiResponse>('/consumer/address/add', payload)
}

// 更新地址
export function updateAddress(id: number, data: AddressDTO) {
  // 转换 isDefault: boolean → number (0/1) 给后端
  const payload = {
    ...data,
    isDefault: data.isDefault ? 1 : 0
  }
  return request.put<ApiResponse>(`/consumer/address/update/${id}`, payload)
}

// 删除地址
export function deleteAddress(id: number) {
  return request.delete<ApiResponse>(`/consumer/address/delete/${id}`)
}

// 设置默认地址
export function setDefaultAddress(id: number) {
  return request.put<ApiResponse>(`/consumer/address/setDefault/${id}`)
}

// 获取默认地址
export function getDefaultAddress() {
  return request.get<ApiResponse<Address>>('/consumer/address/default')
}
