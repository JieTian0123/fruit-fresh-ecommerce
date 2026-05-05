import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'

// Mock dependencies before importing store
vi.mock('@/api/user', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
}))

vi.mock('@/api/request', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
  },
  setLoggingOut: vi.fn(),
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
}))

vi.mock('@/router', () => ({
  default: {
    push: vi.fn().mockResolvedValue(undefined),
    currentRoute: { value: { path: '/' } },
  },
}))

// Mock cart store used in logout
vi.mock('@/stores/cart', () => ({
  useCartStore: vi.fn(() => ({
    resetCart: vi.fn(),
  })),
}))

import { useUserStore } from '@/stores/user'
import * as userApi from '@/api/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    localStorage.clear()
  })

  describe('initial state', () => {
    it('should have empty token when localStorage is empty', () => {
      const store = useUserStore()
      expect(store.token).toBe('')
    })

    it('should have null userInfo initially', () => {
      const store = useUserStore()
      expect(store.userInfo).toBeNull()
    })

    it('should have loading false', () => {
      const store = useUserStore()
      expect(store.loading).toBe(false)
    })

    it('should not be logged in initially', () => {
      const store = useUserStore()
      expect(store.isLoggedIn).toBe(false)
    })
  })

  describe('computed properties', () => {
    it('should detect consumer role', () => {
      const store = useUserStore()
      store.userInfo = {
        id: 1, username: 'test', phone: '', email: '', avatar: '',
        role: 'CONSUMER', status: 1, createTime: ''
      }
      expect(store.isConsumer).toBe(true)
      expect(store.isMerchant).toBe(false)
      expect(store.isAdmin).toBe(false)
    })

    it('should detect merchant role', () => {
      const store = useUserStore()
      store.userInfo = {
        id: 1, username: 'merchant1', phone: '', email: '', avatar: '',
        role: 'MERCHANT', status: 1, createTime: ''
      }
      expect(store.isMerchant).toBe(true)
      expect(store.isConsumer).toBe(false)
    })

    it('should detect admin role', () => {
      const store = useUserStore()
      store.userInfo = {
        id: 1, username: 'admin', phone: '', email: '', avatar: '',
        role: 'ADMIN', status: 1, createTime: ''
      }
      expect(store.isAdmin).toBe(true)
    })

    it('should return username from userInfo', () => {
      const store = useUserStore()
      store.userInfo = {
        id: 1, username: 'testuser', phone: '', email: '', avatar: '',
        role: 'CONSUMER', status: 1, createTime: ''
      }
      expect(store.username).toBe('testuser')
    })

    it('should return empty string for username when no userInfo', () => {
      const store = useUserStore()
      expect(store.username).toBe('')
    })

    it('should return avatar from userInfo', () => {
      const store = useUserStore()
      store.userInfo = {
        id: 1, username: 'test', phone: '', email: '', avatar: 'http://avatar.jpg',
        role: 'CONSUMER', status: 1, createTime: ''
      }
      expect(store.avatar).toBe('http://avatar.jpg')
    })
  })

  describe('initUserInfo', () => {
    it('should load userInfo from localStorage on creation', () => {
      const info = {
        id: 1, username: 'stored', phone: '', email: '', avatar: '',
        role: 'CONSUMER', status: 1, createTime: ''
      }
      localStorage.setItem('userInfo', JSON.stringify(info))

      const store = useUserStore()
      expect(store.userInfo).toEqual(info)
    })

    it('should handle invalid JSON in localStorage', () => {
      localStorage.setItem('userInfo', 'invalid-json')

      const store = useUserStore()
      expect(store.userInfo).toBeNull()
    })
  })

  describe('login', () => {
    it('should login consumer and redirect to home', async () => {
      vi.mocked(userApi.login).mockResolvedValue({
        data: {
          token: 'test-token',
          userId: 1,
          username: 'testuser',
          avatar: 'avatar.jpg',
          role: 'CONSUMER',
        },
      } as any)

      const store = useUserStore()
      const result = await store.login('testuser', 'password', 'captcha', 'uuid')

      expect(result).toBe(true)
      expect(store.token).toBe('test-token')
      expect(store.userInfo?.username).toBe('testuser')
      expect(store.userInfo?.role).toBe('CONSUMER')
      expect(localStorage.getItem('token')).toBe('test-token')
      expect(ElMessage.success).toHaveBeenCalledWith('登录成功')
      expect(router.push).toHaveBeenCalledWith('/')
    })

    it('should redirect admin to /admin', async () => {
      vi.mocked(userApi.login).mockResolvedValue({
        data: {
          token: 'admin-token', userId: 2, username: 'admin',
          avatar: '', role: 'ADMIN',
        },
      } as any)

      const store = useUserStore()
      await store.login('admin', 'pass', 'c', 'u')

      expect(router.push).toHaveBeenCalledWith('/admin')
    })

    it('should redirect merchant to /merchant', async () => {
      vi.mocked(userApi.login).mockResolvedValue({
        data: {
          token: 'merchant-token', userId: 3, username: 'merchant',
          avatar: '', role: 'MERCHANT',
        },
      } as any)

      const store = useUserStore()
      await store.login('merchant', 'pass', 'c', 'u')

      expect(router.push).toHaveBeenCalledWith('/merchant')
    })

    it('should return false on login failure', async () => {
      vi.mocked(userApi.login).mockRejectedValue(new Error('Invalid credentials'))

      const store = useUserStore()
      const result = await store.login('bad', 'pass', 'c', 'u')

      expect(result).toBe(false)
      expect(store.loading).toBe(false)
    })
  })

  describe('logout', () => {
    it('should clear state and redirect to home', async () => {
      const store = useUserStore()
      store.token = 'test-token'
      store.userInfo = {
        id: 1, username: 'test', phone: '', email: '', avatar: '',
        role: 'CONSUMER', status: 1, createTime: ''
      }
      localStorage.setItem('token', 'test-token')
      localStorage.setItem('userInfo', '{}')

      vi.mocked(userApi.logout).mockResolvedValue({} as any)

      await store.logout()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
      expect(localStorage.getItem('userInfo')).toBeNull()
      expect(router.push).toHaveBeenCalledWith('/')
    })

    it('should still clear state even if API call fails', async () => {
      vi.mocked(userApi.logout).mockRejectedValue(new Error('Network error'))

      const store = useUserStore()
      store.token = 'token'
      store.userInfo = {
        id: 1, username: 'test', phone: '', email: '', avatar: '',
        role: 'CONSUMER', status: 1, createTime: ''
      }

      await store.logout()

      expect(store.token).toBe('')
      expect(store.userInfo).toBeNull()
    })
  })

  describe('updateUserInfo', () => {
    it('should merge partial info into existing userInfo', () => {
      const store = useUserStore()
      store.userInfo = {
        id: 1, username: 'test', phone: '123', email: 'a@b.com', avatar: '',
        role: 'CONSUMER', status: 1, createTime: ''
      }

      store.updateUserInfo({ phone: '456', avatar: 'new.jpg' })

      expect(store.userInfo?.phone).toBe('456')
      expect(store.userInfo?.avatar).toBe('new.jpg')
      expect(store.userInfo?.username).toBe('test') // unchanged
    })

    it('should do nothing if userInfo is null', () => {
      const store = useUserStore()
      store.userInfo = null

      store.updateUserInfo({ phone: '456' })

      expect(store.userInfo).toBeNull()
    })
  })

  describe('fetchUserInfo', () => {
    it('should do nothing if no token', async () => {
      const store = useUserStore()
      store.token = ''

      await store.fetchUserInfo()

      expect(userApi.getCurrentUser).not.toHaveBeenCalled()
    })

    it('should fetch and update userInfo', async () => {
      vi.mocked(userApi.getCurrentUser).mockResolvedValue({
        data: {
          userId: 1, username: 'fetched', nickname: 'nick',
          phone: '123', email: 'a@b.com', avatar: 'av.jpg',
          role: 'CONSUMER', status: 1, createTime: '2024-01-01'
        },
      } as any)

      const store = useUserStore()
      store.token = 'valid-token'

      await store.fetchUserInfo()

      expect(store.userInfo?.username).toBe('fetched')
      expect(store.userInfo?.id).toBe(1)
    })
  })
})
