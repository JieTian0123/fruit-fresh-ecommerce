# Common Module (通用模块)

## 简介
`common` 包存放整个项目中通用的基础类、全局标准定义和公共组件。这些类通常被其他所有层（Controller, Service, DAO）所依赖。

## 目录结构
- **result/**: 统一响应结果封装
  - `Result<T>`: 统一 API 返回格式（code, msg, data）。
  - `ResultCode`: 状态码枚举。
- **exception/**: 全局异常处理
  - `GlobalExceptionHandler`: 全局异常捕获处理类。
  - `CustomException`: 自定义业务异常。

## 核心作用
1. **规范交互协议**：通过 `Result` 类确保前后端交互格式的一致性。
2. **统一错误处理**：通过异常拦截器集中处理错误，避免代码中充斥 try-catch。
