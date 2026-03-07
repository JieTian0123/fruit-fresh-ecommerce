import request from './request'
import type { ApiResponse, PageResult, Review, ReviewDTO } from '@/types'

// 提交评价
export function addReview(data: ReviewDTO) {
  return request.post<ApiResponse>('/consumer/review/add', data)
}

// 获取商品评价列表
export function getProductReviews(productId: number, pageNum = 1, pageSize = 10) {
  return request.get<ApiResponse<PageResult<Review>>>('/consumer/review/product/' + productId, {
    params: { pageNum, pageSize }
  })
}

// 检查是否已评价
export function checkReviewed(orderNo: string, productId: number) {
  return request.get<ApiResponse<boolean>>('/consumer/review/check', {
    params: { orderNo, productId }
  })
}
