# Service Module (业务逻辑层)

## 简介
`service` 包承载了系统的核心业务逻辑，起着承上启下（Controller -> Mapper）的作用。

## 目录结构
- **service/**: 定义业务接口（Interface），如 `IUserService`。
- **service/impl/**: 定义业务实现类（Implementation），如 `UserServiceImpl`。

## 核心作用
1. **业务编排**：调用一个或多个 Mapper 完成复杂的业务操作（如：下单 = 扣库存 + 创建订单 + 扣余额）。
2. **事务管理**：通过 `@Transactional` 注解保证数据的一致性。
3. **数据加工**：将 Entity 转化为 VO，或将 DTO 转化为 Entity。
