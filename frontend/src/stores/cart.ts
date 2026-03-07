import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CartItem } from '@/types'
import {
  getCartList,
  addToCart as addToCartApi,
  updateCartItem as updateCartItemApi,
  deleteCartItem as deleteCartItemApi,
  selectCartItem as selectCartItemApi,
  selectAllCartItems as selectAllCartItemsApi,
  clearCart as clearCartApi
} from '@/api/cart'
import { ElMessage } from 'element-plus'

export const useCartStore = defineStore('cart', () => {
  // 状态
  const cartItems = ref<CartItem[]>([])
  const loading = ref(false)

  // 计算属性
  // Badge显示：已选中的不同商品种类数（不是总数量）
  const cartCount = computed(() => {
    return selectedItems.value.length
  })

  const selectedItems = computed(() => {
    return cartItems.value.filter(item => item.selected === 1)
  })

  // 已选中商品的总数量
  const selectedCount = computed(() => {
    return selectedItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  const totalAmount = computed(() => {
    return selectedItems.value.reduce((sum, item) => {
      const price = item.price || 0
      return sum + price * item.quantity
    }, 0)
  })

  const isAllSelected = computed(() => {
    return cartItems.value.length > 0 && cartItems.value.every(item => item.selected === 1)
  })

  // 获取购物车列表
  async function fetchCartList() {
    loading.value = true
    try {
      const res = await getCartList()
      cartItems.value = res.data || []
    } catch {
      cartItems.value = []
    } finally {
      loading.value = false
    }
  }

  // 添加到购物车
  async function addToCart(productId: number, quantity: number = 1) {
    try {
      await addToCartApi({ productId, quantity })
      ElMessage.success('已加入购物车')
      await fetchCartList()
      return true
    } catch {
      return false
    }
  }

  // 更新数量
  async function updateQuantity(id: number, quantity: number) {
    if (quantity < 1) return

    try {
      await updateCartItemApi(id, quantity)
      const item = cartItems.value.find(i => i.id === id)
      if (item) {
        item.quantity = quantity
      }
    } catch (error) {
      ElMessage.error('更新数量失败，请重试')
      // 失败时刷新列表
      await fetchCartList()
    }
  }

  // 删除商品
  async function removeItem(id: number) {
    try {
      await deleteCartItemApi(id)
      cartItems.value = cartItems.value.filter(item => item.id !== id)
      ElMessage.success('已删除')
    } catch (error) {
      ElMessage.error('删除失败，请重试')
      // 失败时刷新列表
      await fetchCartList()
    }
  }

  // 选中/取消选中
  async function toggleSelect(id: number) {
    const item = cartItems.value.find(i => i.id === id)
    if (!item) return

    const newSelected = item.selected === 1 ? 0 : 1
    try {
      await selectCartItemApi(id, newSelected === 1)
      item.selected = newSelected
    } catch (error) {
      ElMessage.error('操作失败，请重试')
      await fetchCartList()
    }
  }

  // 全选/取消全选
  async function toggleSelectAll() {
    const newSelected = isAllSelected.value ? 0 : 1
    try {
      await selectAllCartItemsApi(newSelected === 1)
      cartItems.value.forEach(item => {
        item.selected = newSelected
      })
    } catch (error) {
      ElMessage.error('操作失败，请重试')
      await fetchCartList()
    }
  }

  // 清空购物车
  async function clearAll() {
    try {
      await clearCartApi()
      cartItems.value = []
      ElMessage.success('购物车已清空')
    } catch (error) {
      ElMessage.error('清空失败，请重试')
    }
  }

  // 移除已选中的商品（下单后调用）
  function removeSelectedItems() {
    cartItems.value = cartItems.value.filter(item => item.selected !== 1)
  }

  // 清空购物车数据（退出登录时调用）
  function resetCart() {
    cartItems.value = []
  }

  return {
    cartItems,
    loading,
    cartCount,
    selectedItems,
    selectedCount,
    totalAmount,
    isAllSelected,
    fetchCartList,
    addToCart,
    updateQuantity,
    removeItem,
    toggleSelect,
    toggleSelectAll,
    clearAll,
    removeSelectedItems,
    resetCart
  }
})
