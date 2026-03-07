# Interceptor Module (拦截器模块)

## 简介
`interceptor` 包存放 Spring MVC 的拦截器实现类。

## 核心作用
拦截器用于在请求到达 Controller 之前或之后执行特定的逻辑，是实现**切面编程 (AOP)** 的一种方式。

## 主要功能
- **AuthenticationInterceptor (鉴权拦截器)**:
  - 拦截 HTTP 请求头，解析 Token (如 JWT)。
  - 验证用户登录状态和权限。
  - 将用户信息注入到 `ThreadLocal` 或 Request Context 中，供后续 Controller 使用。
- **LogInterceptor (日志拦截器)**: 记录请求耗时、请求参数等，便于排查问题。
