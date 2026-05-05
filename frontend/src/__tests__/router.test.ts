import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock user store
const mockUserStore = {
  token: '',
  userInfo: null as any,
  fetchUserInfo: vi.fn(),
}

vi.mock('@/stores/user', () => ({
  useUserStore: () => mockUserStore,
}))

// Mock API modules needed by store
vi.mock('@/api/user', () => ({
  login: vi.fn(),
  logout: vi.fn(),
  getCurrentUser: vi.fn(),
}))

vi.mock('@/api/request', () => ({
  default: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
  setLoggingOut: vi.fn(),
}))

vi.mock('element-plus', () => ({
  ElMessage: { success: vi.fn(), error: vi.fn(), warning: vi.fn(), info: vi.fn() },
}))

import { createRouter, createWebHistory, createMemoryHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

// Minimal route config to test guards
const DummyComponent = { template: '<div />' }

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: DummyComponent,
    children: [
      { path: '', name: 'Home', component: DummyComponent, meta: { title: '首页' } },
      { path: 'cart', name: 'Cart', component: DummyComponent, meta: { title: '购物车', requireAuth: true } },
      { path: 'profile', name: 'Profile', component: DummyComponent, meta: { title: '个人中心', requireAuth: true } },
      { path: 'category/:id?', name: 'Category', component: DummyComponent, meta: { title: '分类' } },
    ],
  },
  { path: '/login', name: 'Login', component: DummyComponent, meta: { title: '登录' } },
  { path: '/register', name: 'Register', component: DummyComponent, meta: { title: '注册' } },
  { path: '/merchant/login', name: 'MerchantLogin', component: DummyComponent, meta: { title: '商家登录' } },
  {
    path: '/merchant',
    component: DummyComponent,
    meta: { requireAuth: true, role: 'MERCHANT' },
    children: [
      { path: '', name: 'MerchantDashboard', component: DummyComponent, meta: { title: '商家中心' } },
    ],
  },
  { path: '/admin/login', name: 'AdminLogin', component: DummyComponent, meta: { title: '管理员登录' } },
  {
    path: '/admin',
    component: DummyComponent,
    meta: { requireAuth: true, role: 'ADMIN' },
    children: [
      { path: '', name: 'AdminDashboard', component: DummyComponent, meta: { title: '管理后台' } },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'NotFound', component: DummyComponent, meta: { title: '404' } },
]

function createTestRouter() {
  const router = createRouter({
    history: createMemoryHistory(),
    routes,
  })

  // Replicate the guard logic from src/router/index.ts
  router.beforeEach(async (to, from, next) => {
    document.title = `${to.meta.title || '水果生鲜'} - 水果生鲜电商`

    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')
    let userInfo = null
    try {
      userInfo = userInfoStr && userInfoStr !== 'undefined' ? JSON.parse(userInfoStr) : null
    } catch {
      localStorage.removeItem('userInfo')
      localStorage.removeItem('token')
    }

    // Login page redirect for already-logged-in users
    const loginRouteRoleMap: Record<string, string> = {
      Login: 'CONSUMER',
      Register: 'CONSUMER',
      MerchantLogin: 'MERCHANT',
      AdminLogin: 'ADMIN',
    }
    const targetLoginRole = loginRouteRoleMap[to.name as string]
    if (targetLoginRole && token && userInfo) {
      if (userInfo.role === targetLoginRole) {
        if (userInfo.role === 'ADMIN') {
          next({ name: 'AdminDashboard' })
        } else if (userInfo.role === 'MERCHANT') {
          next({ name: 'MerchantDashboard' })
        } else {
          next({ name: 'Home' })
        }
        return
      }
    }

    const requireAuth = to.meta.requireAuth === true

    if (requireAuth && !token) {
      next({ name: 'Login', query: { redirect: to.fullPath } })
      return
    }

    if (requireAuth && to.meta.role && userInfo?.role !== to.meta.role) {
      if (userInfo?.role === 'ADMIN') {
        next({ name: 'AdminDashboard' })
      } else if (userInfo?.role === 'MERCHANT') {
        next({ name: 'MerchantDashboard' })
      } else {
        next({ name: 'Home' })
      }
      return
    }

    next()
  })

  return router
}

describe('Router Guards', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    localStorage.clear()
  })

  it('should allow access to public routes without auth', async () => {
    const router = createTestRouter()
    await router.push('/')
    expect(router.currentRoute.value.name).toBe('Home')
  })

  it('should allow access to category without auth', async () => {
    const router = createTestRouter()
    await router.push('/category/1')
    expect(router.currentRoute.value.name).toBe('Category')
  })

  it('should redirect to login when accessing auth-required route without token', async () => {
    const router = createTestRouter()
    await router.push('/cart')
    expect(router.currentRoute.value.name).toBe('Login')
    expect(router.currentRoute.value.query.redirect).toBe('/cart')
  })

  it('should redirect to login when accessing profile without token', async () => {
    const router = createTestRouter()
    await router.push('/profile')
    expect(router.currentRoute.value.name).toBe('Login')
  })

  it('should allow access to auth-required route with token', async () => {
    localStorage.setItem('token', 'valid-token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'CONSUMER' }))
    const router = createTestRouter()
    await router.push('/cart')
    expect(router.currentRoute.value.name).toBe('Cart')
  })

  it('should redirect already-logged-in CONSUMER away from /login', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'CONSUMER' }))
    const router = createTestRouter()
    await router.push('/login')
    expect(router.currentRoute.value.name).toBe('Home')
  })

  it('should redirect already-logged-in ADMIN away from /admin/login', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'ADMIN' }))
    const router = createTestRouter()
    await router.push('/admin/login')
    expect(router.currentRoute.value.name).toBe('AdminDashboard')
  })

  it('should redirect already-logged-in MERCHANT away from /merchant/login', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'MERCHANT' }))
    const router = createTestRouter()
    await router.push('/merchant/login')
    expect(router.currentRoute.value.name).toBe('MerchantDashboard')
  })

  it('should allow CONSUMER to access /merchant/login (cross-role login page)', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'CONSUMER' }))
    const router = createTestRouter()
    await router.push('/merchant/login')
    // Consumer != MERCHANT, so should NOT redirect, should allow access
    expect(router.currentRoute.value.name).toBe('MerchantLogin')
  })

  it('should allow ADMIN to access /login (cross-role login page)', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'ADMIN' }))
    const router = createTestRouter()
    await router.push('/login')
    // ADMIN != CONSUMER, so should allow access
    expect(router.currentRoute.value.name).toBe('Login')
  })

  it('should redirect CONSUMER trying to access MERCHANT routes', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'CONSUMER' }))
    const router = createTestRouter()
    await router.push('/merchant')
    // Consumer cannot access merchant routes, redirect to Home
    expect(router.currentRoute.value.name).toBe('Home')
  })

  it('should redirect CONSUMER trying to access ADMIN routes', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'CONSUMER' }))
    const router = createTestRouter()
    await router.push('/admin')
    expect(router.currentRoute.value.name).toBe('Home')
  })

  it('should redirect MERCHANT trying to access ADMIN routes', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'MERCHANT' }))
    const router = createTestRouter()
    await router.push('/admin')
    expect(router.currentRoute.value.name).toBe('MerchantDashboard')
  })

  it('should set document title from route meta', async () => {
    const router = createTestRouter()
    await router.push('/')
    expect(document.title).toBe('首页 - 水果生鲜电商')
  })

  it('should set document title for login page', async () => {
    const router = createTestRouter()
    await router.push('/login')
    expect(document.title).toBe('登录 - 水果生鲜电商')
  })

  it('should handle invalid JSON in localStorage gracefully', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', 'invalid-json')
    const router = createTestRouter()
    // Should not throw, should clear invalid data
    await router.push('/cart')
    expect(localStorage.getItem('userInfo')).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
  })

  it('should allow MERCHANT to access merchant dashboard', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'MERCHANT' }))
    const router = createTestRouter()
    await router.push('/merchant')
    expect(router.currentRoute.value.name).toBe('MerchantDashboard')
  })

  it('should allow ADMIN to access admin dashboard', async () => {
    localStorage.setItem('token', 'token')
    localStorage.setItem('userInfo', JSON.stringify({ role: 'ADMIN' }))
    const router = createTestRouter()
    await router.push('/admin')
    expect(router.currentRoute.value.name).toBe('AdminDashboard')
  })

  it('should redirect unauthenticated user from merchant route to login', async () => {
    const router = createTestRouter()
    await router.push('/merchant')
    expect(router.currentRoute.value.name).toBe('Login')
    expect(router.currentRoute.value.query.redirect).toBe('/merchant')
  })

  it('should redirect unauthenticated user from admin route to login', async () => {
    const router = createTestRouter()
    await router.push('/admin')
    expect(router.currentRoute.value.name).toBe('Login')
    expect(router.currentRoute.value.query.redirect).toBe('/admin')
  })
})
