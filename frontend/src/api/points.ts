import request from '@/api/request'
import type { ApiResponse, PageResult } from '@/types'
import type { UserPointsLog, UserSignIn, MemberLevel, SignInStatus } from '@/types'

/**
 * 签到相关API
 */

// 签到
export function signIn() {
  return request.post<ApiResponse<UserSignIn>>('/consumer/sign-in')
}

// 获取签到状态
export function getSignInStatus() {
  return request.get<ApiResponse<SignInStatus>>('/consumer/sign-in/status')
}

// 获取本月签到记录
export function getMonthSignIns(year?: number, month?: number) {
  return request.get<ApiResponse<UserSignIn[]>>('/consumer/sign-in/month', {
    params: { year, month }
  })
}

// 获取会员等级列表
export function getMemberLevels() {
  return request.get<ApiResponse<MemberLevel[]>>('/consumer/sign-in/levels')
}

// 获取当前会员等级
export function getCurrentLevel() {
  return request.get<ApiResponse<MemberLevel>>('/consumer/sign-in/level')
}

/**
 * 积分相关API
 */

// 获取积分记录
export function getPointsLog(pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<PageResult<UserPointsLog>>>('/consumer/points/log', {
    params: { pageNum, pageSize }
  })
}

// 获取当前积分余额
export function getPointsBalance() {
  return request.get<ApiResponse<number>>('/consumer/points/balance')
}
