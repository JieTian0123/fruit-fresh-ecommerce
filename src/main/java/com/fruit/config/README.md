# Config Module (配置模块)

## 简介
`config` 包存放 Spring Boot 应用的各种配置类，用于定制化框架行为和集成第三方库。

## 常见配置
*注：具体包含的配置类视项目实际情况而定，常见如下：*
- **WebMvcConfig**: 配置拦截器（Interceptor）、跨域（CORS）、静态资源映射等。
- **MyBatisPlusConfig**: 配置 MyBatis-Plus 分页插件、性能分析插件等。
- **SwaggerConfig / Knife4jConfig**: 配置 API 文档生成。
- **JacksonConfig**: 配置 JSON 序列化策略（如日期格式化）。

## 核心作用
本模块是系统的“控制面板”，通过 Java Configuration (注解配置) 的方式来管理系统行为，替代繁琐的 XML 配置。
