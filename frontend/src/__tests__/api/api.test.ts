import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'

// We test the API module functions to verify they call the correct endpoints
// Mock the request module
vi.mock('@/api/request', () => {
  const mockRequest = {
    get: vi.fn().mockResolvedValue({ code: 200, data: null, message: 'ok' }),
    post: vi.fn().mockResolvedValue({ code: 200, data: null, message: 'ok' }),
    put: vi.fn().mockResolvedValue({ code: 200, data: null, message: 'ok' }),
    delete: vi.fn().mockResolvedValue({ code: 200, data: null, message: 'ok' }),
    request: vi.fn().mockResolvedValue({ code: 200, data: null, message: 'ok' }),
  }
  return {
    default: mockRequest,
    setLoggingOut: vi.fn(),
  }
})

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
}))

vi.mock('@/router', () => ({
  default: { push: vi.fn(), currentRoute: { value: { path: '/' } } },
}))

import request from '@/api/request'

describe('User API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call login endpoint', async () => {
    const { login } = await import('@/api/user')
    await login({ username: 'test', password: '123', captchaCode: 'abc', captchaUuid: 'uuid' })
    expect(request.post).toHaveBeenCalledWith(
      '/user/login',
      { username: 'test', password: '123', captchaCode: 'abc', captchaUuid: 'uuid' }
    )
  })

  it('should call register endpoint', async () => {
    const { register } = await import('@/api/user')
    await register({ username: 'new', password: '123', phone: '13800000000', role: 'CONSUMER' })
    expect(request.post).toHaveBeenCalledWith(
      '/user/register',
      { username: 'new', password: '123', phone: '13800000000', role: 'CONSUMER' }
    )
  })

  it('should call getCurrentUser endpoint', async () => {
    const { getCurrentUser } = await import('@/api/user')
    await getCurrentUser()
    expect(request.get).toHaveBeenCalledWith('/user/info')
  })

  it('should call logout endpoint', async () => {
    const { logout } = await import('@/api/user')
    await logout()
    expect(request.post).toHaveBeenCalledWith('/user/logout')
  })

  it('should call updatePassword endpoint', async () => {
    const { updatePassword } = await import('@/api/user')
    await updatePassword({ oldPassword: 'old', newPassword: 'new' })
    expect(request.put).toHaveBeenCalledWith('/user/password', { oldPassword: 'old', newPassword: 'new' })
  })

  it('should call merchantLogin endpoint', async () => {
    const { merchantLogin } = await import('@/api/user')
    await merchantLogin({ username: 'merchant', password: '123', captchaCode: 'abc', captchaUuid: 'uuid' })
    expect(request.post).toHaveBeenCalledWith(
      '/merchant/login',
      { username: 'merchant', password: '123', captchaCode: 'abc', captchaUuid: 'uuid' }
    )
  })

  it('should call adminLogin endpoint', async () => {
    const { adminLogin } = await import('@/api/user')
    await adminLogin({ username: 'admin', password: '123', captchaCode: 'abc', captchaUuid: 'uuid' })
    expect(request.post).toHaveBeenCalledWith(
      '/admin/login',
      { username: 'admin', password: '123', captchaCode: 'abc', captchaUuid: 'uuid' }
    )
  })
})

describe('Cart API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call getCartList endpoint', async () => {
    const { getCartList } = await import('@/api/cart')
    await getCartList()
    expect(request.get).toHaveBeenCalledWith('/consumer/cart/list')
  })

  it('should call addToCart endpoint', async () => {
    const { addToCart } = await import('@/api/cart')
    await addToCart({ productId: 1, quantity: 2 })
    expect(request.post).toHaveBeenCalledWith('/consumer/cart/add', { productId: 1, quantity: 2 })
  })

  it('should call updateCartItem endpoint', async () => {
    const { updateCartItem } = await import('@/api/cart')
    await updateCartItem(1, 3)
    expect(request.put).toHaveBeenCalledWith('/consumer/cart/update/1', null, { params: { quantity: 3 } })
  })

  it('should call deleteCartItem endpoint', async () => {
    const { deleteCartItem } = await import('@/api/cart')
    await deleteCartItem(1)
    expect(request.delete).toHaveBeenCalledWith('/consumer/cart/delete/1')
  })

  it('should call selectCartItem endpoint', async () => {
    const { selectCartItem } = await import('@/api/cart')
    await selectCartItem(1, true)
    expect(request.put).toHaveBeenCalledWith('/consumer/cart/select/1', null, { params: { selected: 1 } })
  })

  it('should call selectCartItem with unselected', async () => {
    const { selectCartItem } = await import('@/api/cart')
    await selectCartItem(1, false)
    expect(request.put).toHaveBeenCalledWith('/consumer/cart/select/1', null, { params: { selected: 0 } })
  })

  it('should call clearCart endpoint', async () => {
    const { clearCart } = await import('@/api/cart')
    await clearCart()
    expect(request.delete).toHaveBeenCalledWith('/consumer/cart/clear')
  })
})

describe('Product API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call getProductList endpoint', async () => {
    const { getProductList } = await import('@/api/product')
    await getProductList({ pageNum: 1, pageSize: 12 })
    expect(request.get).toHaveBeenCalledWith('/consumer/product/list', { params: { pageNum: 1, pageSize: 12 } })
  })

  it('should call getProductDetail endpoint', async () => {
    const { getProductDetail } = await import('@/api/product')
    await getProductDetail(42)
    expect(request.get).toHaveBeenCalledWith('/consumer/product/42')
  })

  it('should call searchProducts endpoint', async () => {
    const { searchProducts } = await import('@/api/product')
    await searchProducts('苹果', 1, 12)
    expect(request.get).toHaveBeenCalledWith('/consumer/product/search', {
      params: { keyword: '苹果', pageNum: 1, pageSize: 12 }
    })
  })

  it('should call getHotProducts endpoint', async () => {
    const { getHotProducts } = await import('@/api/product')
    await getHotProducts(8)
    expect(request.get).toHaveBeenCalledWith('/consumer/product/hot', { params: { limit: 8 } })
  })

  it('should call getNewProducts endpoint', async () => {
    const { getNewProducts } = await import('@/api/product')
    await getNewProducts(8)
    expect(request.get).toHaveBeenCalledWith('/consumer/product/new', { params: { limit: 8 } })
  })

  it('should call getProductTraceability endpoint', async () => {
    const { getProductTraceability } = await import('@/api/product')
    await getProductTraceability(1)
    expect(request.get).toHaveBeenCalledWith('/consumer/product/1/traceability')
  })
})

describe('Order API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call createOrder endpoint', async () => {
    const { createOrder } = await import('@/api/order')
    await createOrder({ addressId: 1, cartItemIds: [1, 2] })
    expect(request.post).toHaveBeenCalledWith('/consumer/order/create', { addressId: 1, cartItemIds: [1, 2] })
  })

  it('should call getOrderList endpoint', async () => {
    const { getOrderList } = await import('@/api/order')
    await getOrderList({ pageNum: 1, pageSize: 10 })
    expect(request.get).toHaveBeenCalledWith('/consumer/order/list', { params: { pageNum: 1, pageSize: 10 } })
  })

  it('should call payOrder endpoint', async () => {
    const { payOrder } = await import('@/api/order')
    await payOrder('ORD123')
    expect(request.put).toHaveBeenCalledWith('/consumer/order/pay/ORD123')
  })

  it('should call cancelOrder endpoint', async () => {
    const { cancelOrder } = await import('@/api/order')
    await cancelOrder('ORD123')
    expect(request.put).toHaveBeenCalledWith('/consumer/order/cancel/ORD123')
  })

  it('should call confirmReceive endpoint', async () => {
    const { confirmReceive } = await import('@/api/order')
    await confirmReceive('ORD123')
    expect(request.put).toHaveBeenCalledWith('/consumer/order/confirm/ORD123')
  })
})

describe('Address API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call getAddressList endpoint', async () => {
    const { getAddressList } = await import('@/api/address')
    await getAddressList()
    expect(request.get).toHaveBeenCalledWith('/consumer/address/list')
  })

  it('should call addAddress with isDefault converted to number', async () => {
    const { addAddress } = await import('@/api/address')
    await addAddress({
      receiverName: '张三',
      receiverPhone: '13800000000',
      province: '北京',
      city: '北京',
      district: '朝阳区',
      detailAddress: '某街道123号',
      isDefault: true,
    })
    expect(request.post).toHaveBeenCalledWith('/consumer/address/add', expect.objectContaining({
      receiverName: '张三',
      isDefault: 1,
    }))
  })

  it('should call addAddress with isDefault false -> 0', async () => {
    const { addAddress } = await import('@/api/address')
    await addAddress({
      receiverName: '李四',
      receiverPhone: '13900000000',
      province: '上海',
      city: '上海',
      district: '浦东新区',
      detailAddress: '某路456号',
      isDefault: false,
    })
    expect(request.post).toHaveBeenCalledWith('/consumer/address/add', expect.objectContaining({
      isDefault: 0,
    }))
  })

  it('should call deleteAddress endpoint', async () => {
    const { deleteAddress } = await import('@/api/address')
    await deleteAddress(5)
    expect(request.delete).toHaveBeenCalledWith('/consumer/address/delete/5')
  })

  it('should call setDefaultAddress endpoint', async () => {
    const { setDefaultAddress } = await import('@/api/address')
    await setDefaultAddress(3)
    expect(request.put).toHaveBeenCalledWith('/consumer/address/setDefault/3')
  })
})

describe('Coupon API', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('should call getAvailableCoupons endpoint', async () => {
    const { getAvailableCoupons } = await import('@/api/coupon')
    await getAvailableCoupons()
    expect(request.get).toHaveBeenCalledWith('/coupon/available')
  })

  it('should call receiveCoupon endpoint', async () => {
    const { receiveCoupon } = await import('@/api/coupon')
    await receiveCoupon(1)
    expect(request.post).toHaveBeenCalledWith('/coupon/receive/1')
  })

  it('should call getMyCoupons with status', async () => {
    const { getMyCoupons } = await import('@/api/coupon')
    await getMyCoupons(1)
    expect(request.get).toHaveBeenCalledWith('/coupon/my', { params: { status: 1 } })
  })

  it('should call getMyCoupons without status', async () => {
    const { getMyCoupons } = await import('@/api/coupon')
    await getMyCoupons()
    expect(request.get).toHaveBeenCalledWith('/coupon/my', { params: {} })
  })

  it('should call exchangeCoupon endpoint', async () => {
    const { exchangeCoupon } = await import('@/api/coupon')
    await exchangeCoupon(5)
    expect(request.post).toHaveBeenCalledWith('/coupon/exchange/5')
  })
})
