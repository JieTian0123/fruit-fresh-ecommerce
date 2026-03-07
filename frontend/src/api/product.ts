import request from './request'
import type { ApiResponse, PageResult, Product, ProductQuery, Category, ProductTraceability } from '@/types'

// 获取商品列表（分页）
export function getProductList(params: ProductQuery) {
  return request.get<ApiResponse<PageResult<Product>>>('/consumer/product/list', { params })
}

// 获取商品详情
export function getProductDetail(id: number) {
  return request.get<ApiResponse<Product>>(`/consumer/product/${id}`)
}

// 搜索商品
export function searchProducts(keyword: string, pageNum = 1, pageSize = 12) {
  return request.get<ApiResponse<PageResult<Product>>>('/consumer/product/search', {
    params: { keyword, pageNum, pageSize }
  })
}

// 获取分类列表
export function getCategoryList() {
  return request.get<ApiResponse<Category[]>>('/consumer/category/list')
}

// 获取分类下的商品
export function getProductsByCategory(categoryId: number, pageNum = 1, pageSize = 12) {
  return request.get<ApiResponse<PageResult<Product>>>('/consumer/product/list', {
    params: { categoryId, pageNum, pageSize }
  })
}

// 获取热销商品
export function getHotProducts(limit = 8) {
  return request.get<ApiResponse<Product[]>>('/consumer/product/hot', { params: { limit } })
}

// 获取新品
export function getNewProducts(limit = 8) {
  return request.get<ApiResponse<Product[]>>('/consumer/product/new', { params: { limit } })
}

// 获取推荐商品
export function getRecommendProducts(limit = 8) {
  return request.get<ApiResponse<Product[]>>('/consumer/product/recommend', { params: { limit } })
}

// ===== 商家商品接口 =====

// 添加商品
export function addProduct(data: Partial<Product>) {
  return request.post<ApiResponse<Product>>('/merchant/product/add', data)
}

// 更新商品
export function updateProduct(id: number, data: Partial<Product>) {
  return request.put<ApiResponse>(`/merchant/product/update/${id}`, data)
}

// 删除商品
export function deleteProduct(id: number) {
  return request.delete<ApiResponse>(`/merchant/product/delete/${id}`)
}

// 获取商品溯源信息
export function getProductTraceability(productId: number) {
  return request.get<ApiResponse<ProductTraceability[]>>(
    `/consumer/product/${productId}/traceability`
  )
}
