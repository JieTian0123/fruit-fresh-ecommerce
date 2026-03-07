import request from './request'
import type { ApiResponse, LoginDTO, RegisterDTO, UserInfo, MerchantLoginResponse, AdminLoginResponse, LoginResponse } from '@/types'

// 用户登录（消费者）
export function login(data: LoginDTO) {
  return request.post<ApiResponse<LoginResponse>>('/user/login', data)
}

// 商家登录
export function merchantLogin(data: LoginDTO) {
  return request.post<ApiResponse<MerchantLoginResponse>>('/merchant/login', data)
}

// 管理员登录
export function adminLogin(data: LoginDTO) {
  return request.post<ApiResponse<AdminLoginResponse>>('/admin/login', data)
}

// 用户注册
export function register(data: RegisterDTO) {
  return request.post<ApiResponse>('/user/register', data)
}

// 商家注册
export function merchantRegister(data: RegisterDTO) {
  return request.post<ApiResponse>('/merchant/register', data)
}

// 获取当前用户信息
export function getCurrentUser() {
  return request.get<ApiResponse<UserInfo>>('/user/info')
}

// 更新用户信息
export function updateUserInfo(data: Partial<UserInfo>) {
  return request.put<ApiResponse>('/user/update', data)
}

// 修改密码
export function updatePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put<ApiResponse>('/user/password', data)
}

// 退出登录
export function logout() {
  return request.post<ApiResponse>('/user/logout')
}

