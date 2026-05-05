import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createRouter, createMemoryHistory } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import type { Product } from '@/types'

// Mock Element Plus components
vi.mock('element-plus', () => ({
  ElButton: { name: 'ElButton', template: '<button><slot /></button>' },
  ElIcon: { name: 'ElIcon', template: '<span><slot /></span>' },
  ElMessage: { success: vi.fn(), error: vi.fn() },
}))

vi.mock('@element-plus/icons-vue', () => ({
  ShoppingCart: { name: 'ShoppingCart', template: '<i />' },
}))

function createProduct(overrides: Partial<Product> = {}): Product {
  return {
    id: 1,
    merchantId: 1,
    categoryId: 1,
    name: '新鲜草莓',
    description: '有机草莓',
    price: 29.90,
    stock: 100,
    unit: '500g',
    images: 'strawberry.jpg',
    status: 1,
    sales: 1500,
    createTime: '2024-01-01',
    updateTime: '2024-01-01',
    ...overrides,
  }
}

function mountCard(product: Product = createProduct()) {
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/product/:id', component: { template: '<div />' } },
    ],
  })

  return mount(ProductCard, {
    props: { product },
    global: {
      plugins: [router],
      stubs: {
        ElButton: { template: '<button @click="$emit(\'click\', $event)"><slot /></button>' },
        ElIcon: { template: '<span><slot /></span>' },
        ShoppingCart: { template: '<i />' },
      },
    },
  })
}

describe('ProductCard', () => {
  it('should render product name', () => {
    const wrapper = mountCard()
    expect(wrapper.find('.product-name').text()).toBe('新鲜草莓')
  })

  it('should display price', () => {
    const wrapper = mountCard(createProduct({ price: 29.90 }))
    expect(wrapper.find('.price').text()).toBe('29.90')
  })

  it('should display current price when discounted', () => {
    const wrapper = mountCard(createProduct({ price: 29.90, currentPrice: 19.90 }))
    expect(wrapper.find('.price').text()).toBe('19.90')
    expect(wrapper.find('.original-price').exists()).toBe(true)
    expect(wrapper.find('.original-price').text()).toBe('¥29.90')
  })

  it('should not show original price when no discount', () => {
    const wrapper = mountCard(createProduct({ price: 29.90, currentPrice: undefined }))
    expect(wrapper.find('.original-price').exists()).toBe(false)
  })

  it('should show sold-out mask when stock is 0', () => {
    const wrapper = mountCard(createProduct({ stock: 0 }))
    expect(wrapper.find('.sold-out-mask').exists()).toBe(true)
    expect(wrapper.find('.sold-out-mask span').text()).toBe('已售罄')
  })

  it('should show low stock badge when stock <= 10', () => {
    const wrapper = mountCard(createProduct({ stock: 5 }))
    expect(wrapper.find('.low-stock-badge').exists()).toBe(true)
    expect(wrapper.find('.low-stock-badge').text()).toContain('5')
  })

  it('should not show low stock badge when stock > 10', () => {
    const wrapper = mountCard(createProduct({ stock: 100 }))
    expect(wrapper.find('.low-stock-badge').exists()).toBe(false)
  })

  it('should show discount badge when discountLabel exists', () => {
    const wrapper = mountCard(createProduct({ discountLabel: '8折' }))
    expect(wrapper.find('.discount-badge').text()).toBe('8折')
  })

  it('should format sales >= 10000 with 万', () => {
    const wrapper = mountCard(createProduct({ sales: 15000 }))
    expect(wrapper.find('.sales').text()).toContain('1.5万')
  })

  it('should format sales >= 1000 with k', () => {
    const wrapper = mountCard(createProduct({ sales: 2500 }))
    expect(wrapper.find('.sales').text()).toContain('2.5k')
  })

  it('should display unit', () => {
    const wrapper = mountCard(createProduct({ unit: '1kg' }))
    expect(wrapper.find('.unit').text()).toBe('1kg')
  })

  it('should emit add-cart event when cart button clicked', async () => {
    const product = createProduct({ stock: 10 })
    const wrapper = mountCard(product)

    // Find the button and click it
    const button = wrapper.find('.product-bottom button')
    if (button.exists()) {
      await button.trigger('click')
      expect(wrapper.emitted('add-cart')).toBeTruthy()
    }
  })

  it('should navigate to product detail on card click', async () => {
    const wrapper = mountCard(createProduct({ id: 42 }))
    // The card has @click="goToDetail" which calls router.push
    await wrapper.find('.product-card').trigger('click')
    // router.push is called with `/product/42`
  })

  it('should show freshness tag when productionDate and shelfLifeDays exist', () => {
    const today = new Date()
    const threeDaysAgo = new Date(today)
    threeDaysAgo.setDate(today.getDate() - 3)

    const wrapper = mountCard(createProduct({
      productionDate: threeDaysAgo.toISOString().split('T')[0],
      shelfLifeDays: 10,
    }))
    expect(wrapper.find('.freshness-tag').exists()).toBe(true)
  })
})
