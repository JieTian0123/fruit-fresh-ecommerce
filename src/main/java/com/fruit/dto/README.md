# DTO Module (Data Transfer Object)

## 简介
`dto` (Data Transfer Object) 包用于存放数据传输对象。这里主要定义**前端主要发送给后端的数据结构**（Request Body）。

## 命名规范
通常以 `DTO` 结尾，如 `UserLoginDTO`, `ProductCreateDTO`。

## 核心作用
1. **参数封装**：将前端传递的 JSON 参数映射为 Java 对象。
2. **数据隔离**：与数据库实体 `Entity` 区分开，避免直接将数据库字段暴露给接口，增强安全性。
3. **参数校验**：通常配合 `@Valid` (Hibernate Validator) 注解进行参数格式校验（如 `@NotNull`, `@Size`）。
