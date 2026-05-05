import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/stores/user'

// 布局组件
const ConsumerLayout = () => import('@/layouts/ConsumerLayout.vue')
const MerchantLayout = () => import('@/layouts/MerchantLayout.vue')
const AdminLayout = () => import('@/layouts/AdminLayout.vue')

// 路由配置
const routes: RouteRecordRaw[] = [
  // 消费者端路由
  {
    path: '/',
    component: ConsumerLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/consumer/HomeView.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'category/:id?',
        name: 'Category',
        component: () => import('@/views/consumer/CategoryView.vue'),
        meta: { title: '商品分类' }
      },
      {
        path: 'activity/:code',
        name: 'ActivityProducts',
        component: () => import('@/views/consumer/ActivityProductsView.vue'),
        meta: { title: '首页活动' }
      },
      {
        path: 'product/:id',
        name: 'ProductDetail',
        component: () => import('@/views/consumer/ProductDetailView.vue'),
        meta: { title: '商品详情' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/consumer/SearchView.vue'),
        meta: { title: '搜索结果' }
      },
      {
        path: 'cart',
        name: 'Cart',
        component: () => import('@/views/consumer/CartView.vue'),
        meta: { title: '购物车', requireAuth: true }
      },
      {
        path: 'checkout',
        name: 'Checkout',
        component: () => import('@/views/consumer/CheckoutView.vue'),
        meta: { title: '确认订单', requireAuth: true }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: () => import('@/views/consumer/OrderListView.vue'),
        meta: { title: '我的订单', requireAuth: true }
      },
      {
        path: 'order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/consumer/OrderDetailView.vue'),
        meta: { title: '订单详情', requireAuth: true }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/consumer/ProfileView.vue'),
        meta: { title: '个人中心', requireAuth: true }
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/consumer/AddressView.vue'),
        meta: { title: '收货地址', requireAuth: true }
      },
      {
        path: 'messages',
        name: 'Messages',
        component: () => import('@/views/consumer/MessagesView.vue'),
        meta: { title: '我的消息', requireAuth: true }
      },
      {
        path: 'chat/:merchantId',
        name: 'Chat',
        component: () => import('@/views/consumer/ChatView.vue'),
        meta: { title: '联系客服', requireAuth: true }
      },
      {
        path: 'member',
        name: 'Member',
        component: () => import('@/views/consumer/MemberView.vue'),
        meta: { title: '会员中心', requireAuth: true }
      },
      {
        path: 'member/sign-in',
        name: 'SignInCalendar',
        component: () => import('@/views/consumer/SignInCalendarView.vue'),
        meta: { title: '签到日历', requireAuth: true }
      },
      {
        path: 'member/vip',
        name: 'VipCenter',
        component: () => import('@/views/consumer/VipCenterView.vue'),
        meta: { title: 'VIP会员特权中心', requireAuth: true }
      },
      {
        path: 'member/points-log',
        name: 'PointsLog',
        component: () => import('@/views/consumer/PointsLogView.vue'),
        meta: { title: '积分记录', requireAuth: true }
      },
      {
        path: 'coupons',
        name: 'Coupons',
        component: () => import('@/views/consumer/CouponView.vue'),
        meta: { title: '优惠券中心', requireAuth: true }
      },
      {
        path: 'point-exchange',
        name: 'PointExchange',
        component: () => import('@/views/consumer/PointExchangeView.vue'),
        meta: { title: '积分兑换', requireAuth: true }
      },
      {
        path: 'announcements',
        name: 'Announcements',
        component: () => import('@/views/consumer/AnnouncementListView.vue'),
        meta: { title: '平台公告' }
      },
      {
        path: 'announcement/:id',
        name: 'AnnouncementDetail',
        component: () => import('@/views/consumer/AnnouncementDetailView.vue'),
        meta: { title: '公告详情' }
      },
      {
        path: 'follow-shops',
        name: 'FollowShops',
        component: () => import('@/views/consumer/MyFollowShopsView.vue'),
        meta: { title: '关注的店铺', requireAuth: true }
      },
      {
        path: 'shop/:id',
        name: 'ShopDetail',
        component: () => import('@/views/consumer/ShopDetailView.vue'),
        meta: { title: '店铺详情' }
      },
    ]
  },

  // 商家登录（必须在商家端路由之前，避免被 /merchant 父路由捕获）
  {
    path: '/merchant/login',
    name: 'MerchantLogin',
    component: () => import('@/views/MerchantLoginView.vue'),
    meta: { title: '商家登录' }
  },
  {
    path: '/merchant/register',
    name: 'MerchantRegister',
    component: () => import('@/views/MerchantRegisterView.vue'),
    meta: { title: '商家注册' }
  },

  // 商家端路由
  {
    path: '/merchant',
    component: MerchantLayout,
    meta: { requireAuth: true, role: 'MERCHANT' },
    children: [
      {
        path: '',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/DashboardView.vue'),
        meta: { title: '商家中心' }
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/ProductListView.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'products/add',
        name: 'MerchantProductAdd',
        component: () => import('@/views/merchant/ProductEditView.vue'),
        meta: { title: '添加商品' }
      },
      {
        path: 'products/edit/:id',
        name: 'MerchantProductEdit',
        component: () => import('@/views/merchant/ProductEditView.vue'),
        meta: { title: '编辑商品' }
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/OrderListView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'shop',
        name: 'MerchantShop',
        component: () => import('@/views/merchant/ShopInfoView.vue'),
        meta: { title: '店铺信息' }
      }
    ]
  },

  // 管理员登录（必须在管理员端路由之前，避免被 /admin 父路由捕获）
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/AdminLoginView.vue'),
    meta: { title: '管理员登录' }
  },

  // 管理员端路由
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requireAuth: true, role: 'ADMIN' },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '管理后台' }
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserListView.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/MerchantListView.vue'),
        meta: { title: '商家管理' }
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/CategoryListView.vue'),
        meta: { title: '分类管理' }
      },
      {
        path: 'products',
        name: 'AdminProducts',
        component: () => import('@/views/admin/ProductListView.vue'),
        meta: { title: '商品管理' }
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/OrderListView.vue'),
        meta: { title: '订单管理' }
      },
      {
        path: 'coupons',
        name: 'AdminCoupons',
        component: () => import('@/views/admin/CouponManageView.vue'),
        meta: { title: '优惠券管理' }
      },
      {
        path: 'activities',
        name: 'AdminActivities',
        component: () => import('@/views/admin/HomeActivityManageView.vue'),
        meta: { title: '首页活动管理' }
      },
      {
        path: 'announcements',
        name: 'AdminAnnouncements',
        component: () => import('@/views/admin/AnnouncementManageView.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'shops',
        name: 'AdminShops',
        component: () => import('@/views/admin/ShopManageView.vue'),
        meta: { title: '店铺管理' }
      },
      {
        path: 'vip',
        name: 'AdminVip',
        component: () => import('@/views/admin/VipManageView.vue'),
        meta: { title: 'VIP管理' }
      }
    ]
  },

  // 登录注册
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: '注册' }
  },

  // 404
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫 - Meta-based认证 + 用户信息hydration
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '水果生鲜'} - 水果生鲜电商`

  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')
  let userInfo = null
  try {
    userInfo = userInfoStr && userInfoStr !== 'undefined' ? JSON.parse(userInfoStr) : null
  } catch (e) {
    // 无效的 JSON，清除 localStorage
    localStorage.removeItem('userInfo')
    localStorage.removeItem('token')
  }

  // 用户信息hydration: token存在但userInfo为空，尝试获取
  const userStore = useUserStore()
  if (token && !userInfo && userStore.token) {
    try {
      await userStore.fetchUserInfo()
      // 重新读取userInfo
      const updatedUserInfoStr = localStorage.getItem('userInfo')
      userInfo = updatedUserInfoStr ? JSON.parse(updatedUserInfoStr) : null
    } catch (error) {
      // fetchUserInfo内部已处理失败情况（清除token并logout）
      // 继续执行后续逻辑
    }
  }

  // 登录页特殊处理：已登录用户访问同角色登录页，跳转到对应首页
  // 允许跨角色访问登录页（如消费者访问 /admin/login 切换身份）
  const loginRouteRoleMap: Record<string, string> = {
    'Login': 'CONSUMER',
    'Register': 'CONSUMER',
    'MerchantLogin': 'MERCHANT',
    'MerchantRegister': 'MERCHANT',
    'AdminLogin': 'ADMIN'
  }
  const targetLoginRole = loginRouteRoleMap[to.name as string]
  if (targetLoginRole && token && userInfo) {
    // 只有当用户角色与目标登录页角色匹配时才跳转（已登录同角色无需再登录）
    if (
      (userInfo.role === 'ADMIN' && targetLoginRole === 'ADMIN') ||
      (userInfo.role === 'MERCHANT' && targetLoginRole === 'MERCHANT') ||
      (userInfo.role === 'CONSUMER' && targetLoginRole === 'CONSUMER')
    ) {
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

  // 检查路由是否需要认证
  const requireAuth = to.meta.requireAuth === true
  
  if (requireAuth && !token) {
    // 需要认证但未登录，跳转登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 角色权限校验（仅当需要认证时）
  if (requireAuth && to.meta.role && userInfo?.role !== to.meta.role) {
    // 无权限，跳转到对应首页
    if (userInfo?.role === 'ADMIN') {
      next({ name: 'AdminDashboard' })
    } else if (userInfo?.role === 'MERCHANT') {
      next({ name: 'MerchantDashboard' })
    } else {
      next({ name: 'Home' })
    }
    return
  }

  // 角色隔离：商家/管理员不能访问消费者页面（非登录页、非404）
  const isConsumerRoute = to.matched.some(r => r.path === '/')
  if (isConsumerRoute && token && userInfo) {
    if (userInfo.role === 'MERCHANT') {
      next({ name: 'MerchantDashboard' })
      return
    }
    if (userInfo.role === 'ADMIN') {
      next({ name: 'AdminDashboard' })
      return
    }
  }

  next()
})

export default router
