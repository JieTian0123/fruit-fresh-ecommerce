// 用户相关类型
export interface UserInfo {
  id: number
  username: string
  nickname?: string
  phone: string
  email: string
  avatar: string
  role: 'CONSUMER' | 'MERCHANT' | 'ADMIN'
  status: number
  createTime: string
}

// 登录响应类型（后端返回扁平结构）
export interface LoginResponse {
  token: string
  userId: number
  username: string
  nickname?: string
  avatar?: string
  role: 'CONSUMER' | 'MERCHANT' | 'ADMIN'
  userType?: string
}

export interface MerchantLoginResponse {
  token: string
  userId: number
  username: string
  nickname?: string
  avatar?: string
  role: 'MERCHANT'
  shopId?: number
  shopName?: string
}

export interface AdminLoginResponse {
  token: string
  userId: number
  username: string
  avatar?: string
  role: 'ADMIN'
}

export interface LoginDTO {
  username: string
  password: string
}

export interface RegisterDTO {
  username: string
  password: string
  phone: string
  email?: string
  role: 'CONSUMER' | 'MERCHANT'
}

// 轮播图类型
export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkUrl: string
  sort: number
  status: number
  createTime?: string
}

// 商品相关类型
export interface Product {
  id: number
  merchantId: number
  shopId?: number
  categoryId: number
  name: string
  description: string
  price: number
  stock: number
  unit: string
  images: string
  status: number
  sales: number
  createTime: string
  updateTime: string
  categoryName?: string
  merchantName?: string
  mainImage?: string  // 主图URL（从images解析或单独返回）
  origin?: string     // 产地
  shelfLifeDays?: number      // 保质期天数
  productionDate?: string     // 生产/采摘日期 (YYYY-MM-DD)
  storageCondition?: string   // 存储条件
  qualityGrade?: string       // 品质等级 A/B/C
  currentPrice?: number       // 动态折扣价（后端计算）
  discountLabel?: string      // 折扣标签（后端返回）
}

export interface ProductQuery {
  categoryId?: number
  keyword?: string
  minPrice?: number
  maxPrice?: number
  sortBy?: 'price' | 'sales' | 'createTime' | string  // 扩展允许任意字符串
  sortOrder?: 'asc' | 'desc' | string  // 允许字符串类型
  status?: number  // 商品状态筛选
  pageNum?: number
  pageSize?: number
}

// 分类相关类型
export interface Category {
  id: number
  name: string
  icon: string
  sort: number
  status: number
  createTime: string
  productCount?: number  // 商品数量（统计字段）
}

// 购物车相关类型 (匹配后端 CartVO)
export interface CartItem {
  id: number
  userId: number
  productId: number
  merchantId?: number
  quantity: number
  selected: number  // 0 或 1
  createTime?: string
  // CartVO 返回的扁平字段
  productName?: string
  productImage?: string
  price?: number
  stock?: number
  // 立即购买模式嵌套的商品对象
  product?: Product
}

export interface CartItemDTO {
  productId: number
  quantity: number
}

// 订单相关类型
export interface Order {
  id: number
  orderNo: string
  userId: number
  merchantId: number
  totalAmount: number
  status: number
  addressId: number
  remark: string
  payTime: string
  deliveryTime: string
  receiveTime: string
  createTime: string
  updateTime: string
  items?: OrderItem[]
  orderItems?: OrderItem[]  // 别名，兼容不同后端返回
  address?: Address
  receiverName?: string     // 地址扁平化字段
  receiverPhone?: string
  receiverAddress?: string
}

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  productImage: string
  price: number
  quantity: number
  totalPrice: number
}

export interface CreateOrderDTO {
  addressId: number
  cartItemIds: number[]
  remark?: string
  orderNo?: string  // 订单号查询参数
}

// 订单查询类型
export interface OrderQuery {
  status?: number
  orderNo?: string  // 订单号筛选
  pageNum?: number
  pageSize?: number
}

// 地址相关类型
export interface Address {
  id: number
  userId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault: boolean
  createTime: string
}

export interface AddressDTO {
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district: string
  detailAddress: string
  isDefault?: boolean | number  // 兼容前端boolean和后端number(0/1)
}

// 评价相关类型
export interface Review {
  id: number
  orderNo: string
  productId: number
  userId: number
  username?: string
  userAvatar?: string
  rating: number
  content: string
  images: string
  reply: string
  replyTime: string
  status: number
  createTime: string
  productName?: string
  productImage?: string
}

export interface ReviewDTO {
  orderNo: string
  productId: number
  rating: number
  content: string
  images?: string
}

// 优惠券类型
export interface Coupon {
  id: number
  title: string
  couponType: number  // 1-满减券 2-折扣券 3-无门槛券
  discountAmount?: number
  discountRate?: number
  minimumAmount?: number
  maximumDiscount?: number
  totalQuantity: number
  receivedQuantity: number
  usedQuantity: number
  perUserLimit: number
  validFrom?: string
  validUntil?: string
  validDays?: number
  applicableCategories?: string
  applicableProducts?: string
  memberLevels?: string
  description?: string
  status: number  // 0-禁用 1-启用 2-已结束
  createTime?: string
  updateTime?: string
}

// 公告类型
export interface Announcement {
  id: number
  type: number  // 1-系统公告 2-活动公告 3-知识科普
  title: string
  content: string
  coverImage?: string
  viewCount: number
  sort: number
  status: number  // 0-草稿 1-发布 2-下架
  publishTime?: string
  createTime?: string
  updateTime?: string
}

// 商家店铺类型
export interface MerchantShop {
  id: number
  merchantId: number
  shopName: string
  logo?: string
  description?: string
  province?: string
  city?: string
  district?: string
  address?: string
  contactPhone: string
  businessLicense?: string
  status: number  // 0-禁用 1-启用 2-待审核
  createTime?: string
  updateTime?: string
}

// 店铺DTO
export interface ShopDTO {
  shopName: string
  logo?: string
  description?: string
  province?: string
  city?: string
  district?: string
  address?: string
  contactPhone: string
  businessLicense?: string
}

// 统计相关类型
export interface DashboardStats {
  totalUsers: number
  totalProducts: number
  totalOrders: number
  totalSales: number
  todayOrders: number
  todaySales: number
}

export interface MerchantStats {
  totalProducts: number
  totalOrders: number
  totalSales: number
  pendingOrders: number
  todayOrders: number
  todaySales: number
}

// API 响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  list?: T[]  // 别名，兼容不同后端返回
  total: number
  size: number
  current: number
  pages: number
}

// 订单状态枚举
export const OrderStatus = {
  PENDING_PAYMENT: 0,    // 待付款
  PENDING_DELIVERY: 1,   // 待发货
  PENDING_RECEIVE: 2,    // 待收货
  COMPLETED: 3,          // 已完成
  CANCELLED: 4,          // 已取消
  REFUNDING: 5,          // 退款中
  REFUNDED: 6            // 已退款
} as const

export const OrderStatusText: Record<number, string> = {
  0: '待付款',
  1: '待发货',
  2: '待收货',
  3: '已完成',
  4: '已取消',
  5: '退款中',
  6: '已退款'
}

export const OrderStatusColor: Record<number, string> = {
  0: 'warning',  // 待付款
  1: 'primary',  // 待发货
  2: 'primary',  // 待收货
  3: 'success',  // 已完成
  4: 'info',     // 已取消
  5: 'warning',  // 退款中
  6: 'info'      // 已退款
}

// 消息类型
export interface Message {
  id: number
  type: 'system' | 'chat'  // 系统通知 或 聊天消息
  senderId?: number
  senderName?: string
  senderAvatar?: string
  receiverId?: number
  content: string
  isRead: boolean
  createTime: string
}

// 会话类型
export interface Conversation {
  id: number
  merchantId: number
  merchantName: string
  merchantAvatar?: string
  shopName?: string
  lastMessage: string
  lastMessageTime: string
  unreadCount: number
}

// 系统通知类型
export interface SystemNotification {
  id: number
  type: 'order' | 'system' | 'promotion'
  title: string
  content: string
  isRead: boolean
  createTime: string
  relatedId?: number  // 关联的订单ID等
}

// 会员等级类型
export interface MemberLevel {
  id: number
  levelName: string
  levelCode: string
  requiredPoints: number
  discountRate: number
  icon?: string
  color?: string
  benefits?: string  // JSON格式
  sort: number
  status: number
}

// 积分记录类型
export interface UserPointsLog {
  id: number
  userId: number
  points: number  // 正数加积分，负数减积分
  sourceType: number  // 1-注册 2-签到 3-消费 4-评价 5-兑换 6-过期 7-管理员调整
  sourceId?: number
  description: string
  balanceAfter: number
  createTime: string
}

// 签到记录类型
export interface UserSignIn {
  id: number
  userId: number
  signDate: string
  continuousDays: number
  pointsEarned: number
  createTime: string
}

// 签到状态类型
export interface SignInStatus {
  signedToday: boolean
  continuousDays: number
}

// 积分来源类型映射
export const PointSourceTypeText: Record<number, string> = {
  1: '注册奖励',
  2: '签到奖励',
  3: '消费奖励',
  4: '评价奖励',
  5: '积分兑换',
  6: '积分过期',
  7: '管理员调整'
}

// 商品溯源类型
export interface ProductTraceability {
  id: number
  productId: number
  nodeType: number          // 1-采摘 2-质检 3-入库 4-出库 5-配送
  nodeName: string
  description?: string
  location?: string
  operator?: string
  temperature?: number
  humidity?: number
  imageUrl?: string
  occurredTime: string
  createTime?: string
}

// 溯源节点类型映射
export const TraceabilityNodeType: Record<number, string> = {
  1: '采摘',
  2: '质检',
  3: '入库',
  4: '出库',
  5: '配送'
}

// VIP套餐类型
export interface VipPlan {
  id: number
  name: string
  description?: string
  price: number
  durationDays: number
  benefits?: string
  sort: number
  status: number  // 0-禁用 1-启用
  createTime?: string
  updateTime?: string
}

// VIP订单类型
export interface VipOrder {
  id: number
  userId: number
  planId?: number
  orderNo: string
  amount: number
  source: number  // 1-购买 2-订单达标自动升级
  status: number  // 1-已付款
  payTime?: string
  vipStartTime?: string
  vipEndTime?: string
  createTime?: string
}

// VIP状态类型
export interface VipStatus {
  isVip: boolean
  expireTime?: string
  completedOrders: number
  upgradeThreshold: number
}
