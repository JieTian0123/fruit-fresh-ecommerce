# Controller Module (控制器模块)

## 简介
`controller` 包是 Web 层的核心，负责接收 HTTP 请求、参数校验、调用 Service 层业务逻辑，并返回统一格式的 JSON 响应。

## 模块划分
本系统按照**用户角色**和**业务场景**进行了清晰的子包划分：

### 1. admin (管理端)
面向平台管理员，提供后台管理功能。
- `UserManageController`: 用户管理（封禁、查看）。
- `CategoryManageController`: 分类管理。
- `SystemConfigController`: 系统设置。

### 2. consumer (消费者端/C端)
面向普通购买用户，提供商城核心体验。
- `HomeController`: 首页数据（轮播图、推荐）。
- `ProductController`: 商品搜索、详情。
- `CartController`: 购物车增删改查。
- `OrderController`: 下单、支付、订单列表。
- `AddressController`: 收货地址管理。

### 3. merchant (商家端/B端)
面向入驻商家，提供店铺运营功能。
- `ProductManageController`: 商品发布、上下架。
- `OrderManageController`: 订单发货、售后处理。
- `DashboardController`: 经营数据概览。

### 4. common (公共接口)
- `FileUploadController`: 文件上传接口（图片等）。
- `AuthController`: 登录、注册、验证码相关接口。
