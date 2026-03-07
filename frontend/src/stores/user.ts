import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '@/api/user'
import { setLoggingOut } from '@/api/request'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { useCartStore } from './cart'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  // 初始化用户信息
  const initUserInfo = () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
      } catch {
        userInfo.value = null
      }
    }
  }

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const isConsumer = computed(() => userInfo.value?.role === 'CONSUMER')
  const isMerchant = computed(() => userInfo.value?.role === 'MERCHANT')
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const username = computed(() => userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')

  // 登录
  async function login(username: string, password: string) {
    loading.value = true
    try {
      const res = await loginApi({ username, password })
      const data = res.data

      // 后端直接返回用户数据在 data 中（不是 data.userInfo）
      // 包含：token, userId, username, nickname, avatar, userType, role
      token.value = data.token
      userInfo.value = {
        id: data.userId,
        username: data.username,
        phone: '',
        email: '',
        avatar: data.avatar || '',
        role: data.role,
        status: 1,
        createTime: ''
      }

      localStorage.setItem('token', data.token)
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))

      ElMessage.success('登录成功')

      // 根据角色跳转
      if (data.role === 'ADMIN') {
        await router.push('/admin')
      } else if (data.role === 'MERCHANT') {
        await router.push('/merchant')
      } else {
        await router.push('/')
      }

      return true
    } catch (error) {
      console.error('登录失败:', error)
      return false
    } finally {
      loading.value = false
    }
  }

  // 登出
  async function logout() {
    setLoggingOut(true)
    try {
      await logoutApi()
    } catch {
      // 忽略错误
    } finally {
      token.value = ''
      userInfo.value = null
      localStorage.removeItem('token')
      localStorage.removeItem('userInfo')
      
      // 清空购物车数据
      const cartStore = useCartStore()
      cartStore.resetCart()
      
      ElMessage.success('已退出登录')
      await router.push('/') // 退出后跳转到首页，允许继续浏览
      
      // 跳转完成后再解除标记，给一点延迟确保不会触发401提示
      setTimeout(() => {
        setLoggingOut(false)
      }, 500)
    }
  }

  // 获取用户信息
  async function fetchUserInfo() {
    if (!token.value) return

    try {
      const res = await getCurrentUser()
      const data = res.data as any
      // 后端返回 LoginVO（含 userId，不含 id），做字段映射
      userInfo.value = {
        id: data.userId ?? data.id,
        username: data.username,
        nickname: data.nickname,
        phone: data.phone || '',
        email: data.email || '',
        avatar: data.avatar || '',
        role: data.role,
        status: data.status ?? 1,
        createTime: data.createTime || ''
      }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    } catch {
      // token 无效，清除登录状态
      logout()
    }
  }

  // 更新用户信息
  function updateUserInfo(info: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...info }
      localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
    }
  }

  // 初始化
  initUserInfo()

  return {
    token,
    userInfo,
    loading,
    isLoggedIn,
    isConsumer,
    isMerchant,
    isAdmin,
    username,
    avatar,
    login,
    logout,
    fetchUserInfo,
    updateUserInfo
  }
})
