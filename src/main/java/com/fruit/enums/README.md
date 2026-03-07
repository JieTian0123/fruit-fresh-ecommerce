# Enums Module (枚举模块)

## 简介
`enums` 包存放系统中的常量定义和枚举类型。

## 常见用途
1. **状态标记**：如 `OrderStatusEnum` (待支付, 待发货, 已完成)。
2. **角色定义**：如 `UserRoleEnum` (消费者, 商家, 管理员)。
3. **业务类型**：如 `PaymentMethodEnum` (微信, 支付宝)。
4. **响应码**：如 `ResultCodeEnum` (成功 200, 失败 500)。

## 核心作用
使用枚举消除代码中的“魔法值”（Magic Numbers/Strings），提高代码的可读性和可维护性。
