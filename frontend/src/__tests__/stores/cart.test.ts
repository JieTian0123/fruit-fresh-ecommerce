import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock all cart API functions
vi.mock('@/api/cart', () => ({
  getCartList: vi.fn(),
  addToCart: vi.fn(),
  updateCartItem: vi.fn(),
  deleteCartItem: vi.fn(),
  selectCartItem: vi.fn(),
  selectAllCartItems: vi.fn(),
  clearCart: vi.fn(),
}))

// Mock Element Plus
vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
}))

import { useCartStore } from '@/stores/cart'
import * as cartApi from '@/api/cart'
import { ElMessage } from 'element-plus'

describe('Cart Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('initial state', () => {
    it('should have empty cartItems', () => {
      const store = useCartStore()
      expect(store.cartItems).toEqual([])
    })

    it('should have loading false', () => {
      const store = useCartStore()
      expect(store.loading).toBe(false)
    })

    it('should have cartCount 0', () => {
      const store = useCartStore()
      expect(store.cartCount).toBe(0)
    })

    it('should have totalAmount 0', () => {
      const store = useCartStore()
      expect(store.totalAmount).toBe(0)
    })
  })

  describe('computed properties', () => {
    it('should compute selectedItems correctly', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 2, selected: 1, price: 10 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 0, price: 20 },
        { id: 3, userId: 1, productId: 3, quantity: 3, selected: 1, price: 15 },
      ] as any
      expect(store.selectedItems).toHaveLength(2)
      expect(store.selectedItems[0].id).toBe(1)
      expect(store.selectedItems[1].id).toBe(3)
    })

    it('should compute cartCount as selected item count', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 2, selected: 1, price: 10 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 0, price: 20 },
      ] as any
      expect(store.cartCount).toBe(1) // only 1 selected
    })

    it('should compute selectedCount as total quantity of selected items', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 2, selected: 1, price: 10 },
        { id: 2, userId: 1, productId: 2, quantity: 3, selected: 1, price: 20 },
      ] as any
      expect(store.selectedCount).toBe(5)
    })

    it('should compute totalAmount correctly', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 2, selected: 1, price: 10 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 0, price: 20 },
        { id: 3, userId: 1, productId: 3, quantity: 3, selected: 1, price: 15 },
      ] as any
      // only selected: 2*10 + 3*15 = 65
      expect(store.totalAmount).toBe(65)
    })

    it('should compute isAllSelected correctly', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 1, selected: 1, price: 10 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 1, price: 20 },
      ] as any
      expect(store.isAllSelected).toBe(true)
    })

    it('should return false for isAllSelected when not all selected', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 1, selected: 1, price: 10 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 0, price: 20 },
      ] as any
      expect(store.isAllSelected).toBe(false)
    })

    it('should return false for isAllSelected when cart is empty', () => {
      const store = useCartStore()
      expect(store.isAllSelected).toBe(false)
    })
  })

  describe('fetchCartList', () => {
    it('should fetch and set cart items', async () => {
      const mockItems = [
        { id: 1, userId: 1, productId: 1, quantity: 2, selected: 1, price: 10 },
      ]
      vi.mocked(cartApi.getCartList).mockResolvedValue({ data: mockItems } as any)

      const store = useCartStore()
      await store.fetchCartList()

      expect(store.cartItems).toEqual(mockItems)
      expect(store.loading).toBe(false)
    })

    it('should handle fetch error gracefully', async () => {
      vi.mocked(cartApi.getCartList).mockRejectedValue(new Error('Network error'))

      const store = useCartStore()
      await store.fetchCartList()

      expect(store.cartItems).toEqual([])
      expect(store.loading).toBe(false)
    })
  })

  describe('addToCart', () => {
    it('should add item and refresh cart', async () => {
      vi.mocked(cartApi.addToCart).mockResolvedValue({} as any)
      vi.mocked(cartApi.getCartList).mockResolvedValue({ data: [] } as any)

      const store = useCartStore()
      const result = await store.addToCart(1, 2)

      expect(result).toBe(true)
      expect(cartApi.addToCart).toHaveBeenCalledWith({ productId: 1, quantity: 2 })
      expect(ElMessage.success).toHaveBeenCalledWith('已加入购物车')
    })

    it('should return false on failure', async () => {
      vi.mocked(cartApi.addToCart).mockRejectedValue(new Error('fail'))

      const store = useCartStore()
      const result = await store.addToCart(1)

      expect(result).toBe(false)
    })
  })

  describe('updateQuantity', () => {
    it('should update quantity locally on success', async () => {
      vi.mocked(cartApi.updateCartItem).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 }] as any

      await store.updateQuantity(1, 5)

      expect(store.cartItems[0].quantity).toBe(5)
    })

    it('should not update if quantity < 1', async () => {
      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 }] as any

      await store.updateQuantity(1, 0)

      expect(cartApi.updateCartItem).not.toHaveBeenCalled()
    })

    it('should refresh cart on failure', async () => {
      vi.mocked(cartApi.updateCartItem).mockRejectedValue(new Error('fail'))
      vi.mocked(cartApi.getCartList).mockResolvedValue({ data: [] } as any)

      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 }] as any

      await store.updateQuantity(1, 5)

      expect(ElMessage.error).toHaveBeenCalledWith('更新数量失败，请重试')
    })
  })

  describe('removeItem', () => {
    it('should remove item from cart on success', async () => {
      vi.mocked(cartApi.deleteCartItem).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 0 },
      ] as any

      await store.removeItem(1)

      expect(store.cartItems).toHaveLength(1)
      expect(store.cartItems[0].id).toBe(2)
      expect(ElMessage.success).toHaveBeenCalledWith('已删除')
    })
  })

  describe('toggleSelect', () => {
    it('should toggle selected state', async () => {
      vi.mocked(cartApi.selectCartItem).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 0 }] as any

      await store.toggleSelect(1)

      expect(store.cartItems[0].selected).toBe(1)
    })

    it('should deselect if already selected', async () => {
      vi.mocked(cartApi.selectCartItem).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 }] as any

      await store.toggleSelect(1)

      expect(store.cartItems[0].selected).toBe(0)
    })

    it('should do nothing if item not found', async () => {
      const store = useCartStore()
      store.cartItems = []

      await store.toggleSelect(999)

      expect(cartApi.selectCartItem).not.toHaveBeenCalled()
    })
  })

  describe('toggleSelectAll', () => {
    it('should select all when not all selected', async () => {
      vi.mocked(cartApi.selectAllCartItems).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 1, selected: 0 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 1 },
      ] as any

      await store.toggleSelectAll()

      expect(store.cartItems.every(i => i.selected === 1)).toBe(true)
    })

    it('should deselect all when all selected', async () => {
      vi.mocked(cartApi.selectAllCartItems).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 1 },
      ] as any

      await store.toggleSelectAll()

      expect(store.cartItems.every(i => i.selected === 0)).toBe(true)
    })
  })

  describe('clearAll', () => {
    it('should clear all items on success', async () => {
      vi.mocked(cartApi.clearCart).mockResolvedValue({} as any)

      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 }] as any

      await store.clearAll()

      expect(store.cartItems).toEqual([])
      expect(ElMessage.success).toHaveBeenCalledWith('购物车已清空')
    })
  })

  describe('removeSelectedItems', () => {
    it('should remove selected items', () => {
      const store = useCartStore()
      store.cartItems = [
        { id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 },
        { id: 2, userId: 1, productId: 2, quantity: 1, selected: 0 },
        { id: 3, userId: 1, productId: 3, quantity: 1, selected: 1 },
      ] as any

      store.removeSelectedItems()

      expect(store.cartItems).toHaveLength(1)
      expect(store.cartItems[0].id).toBe(2)
    })
  })

  describe('resetCart', () => {
    it('should reset cart items', () => {
      const store = useCartStore()
      store.cartItems = [{ id: 1, userId: 1, productId: 1, quantity: 1, selected: 1 }] as any

      store.resetCart()

      expect(store.cartItems).toEqual([])
    })
  })
})
