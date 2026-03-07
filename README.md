# 🍎 水果生鲜电商系统

基于 Spring Boot + Vue 3 的全栈水果生鲜电商平台（单体架构），适用于毕业设计项目。

## 📋 项目简介

本项目是一个完整的 B2B2C 水果生鲜电商系统，采用**前后端分离**架构设计，包含消费者、商家和管理员三种角色，实现了用户管理、商品管理、订单管理、购物车、地址管理等核心电商功能。

**项目特点**：
- ✅ 前后端完全分离，便于独立开发和部署
- ✅ 三端系统（消费者端、商家端、管理员端）统一前端项目
- ✅ RESTful API 设计，标准化接口规范
- ✅ JWT Token 认证，无状态安全认证
- ✅ 统一响应格式，规范的异常处理
- ✅ MyBatis Plus 简化持久层开发
- ✅ Vue 3 Composition API + TypeScript 类型安全

## 🛠 技术栈

### 后端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 1.8 | Java 开发环境 |
| Spring Boot | 2.7.18 | 基础框架 |
| MyBatis Plus | 3.5.5 | ORM 框架，简化 CRUD |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 3.2+ | 缓存 & Token 存储 |
| Druid | 1.2.20 | 阿里数据库连接池，SQL 监控 |
| JWT | 0.9.1 | 无状态身份认证 |
| Knife4j | 3.0.3 | API 文档（Swagger 增强版） |
| Hutool | 5.8.25 | Java 工具类库 |
| Lombok | 1.18.30 | 简化实体类代码 |

### 前端技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.13 | 渐进式前端框架 |
| TypeScript | 5.6+ | 类型安全的 JavaScript 超集 |
| Vite | 6.0+ | 极速构建工具 |
| Vue Router | 4.5+ | 官方路由管理器 |
| Pinia | 2.3+ | 官方状态管理库 |
| Element Plus | 2.9+ | Vue 3 组件库 |
| Axios | 1.7+ | HTTP 请求库 |

## 📁 项目结构

```
fruit-fresh-ecommerce/
├── 📄 README.md                      # 项目总览文档（当前文件）
├── 📄 pom.xml                        # Maven 依赖配置
│
├── 📁 frontend/                      # 前端项目（Vue 3 + TypeScript）
│   ├── 📄 README.md                  # 前端详细文档
│   ├── 📄 package.json               # npm 依赖配置
│   ├── 📄 vite.config.ts             # Vite 构建配置
│   ├── 📄 tsconfig.json              # TypeScript 配置
│   ├── 📁 src/
│   │   ├── 📁 api/                   # API 请求封装
│   │   ├── 📁 views/                 # 页面组件
│   │   │   ├── 📁 consumer/          # 消费者端页面
│   │   │   ├── 📁 merchant/          # 商家端页面
│   │   │   └── 📁 admin/             # 管理员端页面
│   │   ├── 📁 components/            # 公共组件
│   │   ├── 📁 stores/                # Pinia 状态管理
│   │   ├── 📁 router/                # 路由配置
│   │   ├── 📁 utils/                 # 工具函数
│   │   └── 📁 assets/                # 静态资源
│   └── 📁 public/                    # 公共静态文件
│
├── 📁 src/main/java/com/fruit/       # 后端 Java 代码
│   ├── 📄 FruitApplication.java      # Spring Boot 启动类
│   │
│   ├── 📁 controller/                # 控制器层（Web API）
│   │   ├── 📄 README.md              # Controller 层说明
│   │   ├── 📁 consumer/              # 消费者 API（C端）
│   │   ├── 📁 merchant/              # 商家 API（B端）
│   │   └── 📁 admin/                 # 管理员 API
│   │
│   ├── 📁 service/                   # 业务逻辑层
│   │   ├── 📄 README.md              # Service 层说明
│   │   └── 📁 impl/                  # 业务实现类
│   │
│   ├── 📁 mapper/                    # 数据访问层（MyBatis）
│   │   ├── 📄 README.md              # Mapper 层说明
│   │   └── 📁 xml/                   # SQL 映射文件
│   │
│   ├── 📁 entity/                    # 数据库实体类
│   │   └── 📄 README.md              # Entity 说明
│   │
│   ├── 📁 dto/                       # 数据传输对象（请求参数）
│   │   └── 📄 README.md              # DTO 说明
│   │
│   ├── 📁 vo/                        # 视图对象（响应数据）
│   │   └── 📄 README.md              # VO 说明
│   │
│   ├── 📁 config/                    # 配置类
│   │   ├── 📄 README.md              # Config 说明
│   │   ├── WebMvcConfig.java         # Spring MVC 配置
│   │   ├── MyBatisPlusConfig.java    # MyBatis Plus 配置
│   │   └── RedisConfig.java          # Redis 配置
│   │
│   ├── 📁 interceptor/               # 拦截器
│   │   ├── 📄 README.md              # Interceptor 说明
│   │   └── AuthInterceptor.java      # JWT 认证拦截器
│   │
│   ├── 📁 common/                    # 公共模块
│   │   ├── 📄 README.md              # Common 说明
│   │   ├── 📁 result/                # 统一响应格式
│   │   └── 📁 exception/             # 全局异常处理
│   │
│   ├── 📁 utils/                     # 工具类
│   │   ├── 📄 README.md              # Utils 说明
│   │   ├── JwtUtil.java              # JWT 工具类
│   │   └── RedisUtil.java            # Redis 工具类
│   │
│   └── 📁 enums/                     # 枚举类
│       ├── 📄 README.md              # Enums 说明
│       └── UserRole.java             # 用户角色枚举
│
├── 📁 sql/                           # 数据库脚本
│   └── 📄 init.sql                   # 数据库初始化脚本
│
└── 📁 uploads/                       # 文件上传目录（运行时生成）
```

### 目录说明

| 目录 | 说明 | 详细文档 |
|------|------|----------|
| `frontend/` | 前端 Vue 3 项目 | [frontend/README.md](frontend/README.md) |
| `controller/` | Web API 控制器 | [controller/README.md](src/main/java/com/fruit/controller/README.md) |
| `service/` | 业务逻辑层 | [service/README.md](src/main/java/com/fruit/service/README.md) |
| `mapper/` | 数据访问层 | [mapper/README.md](src/main/java/com/fruit/mapper/README.md) |
| `entity/` | 数据库实体 | [entity/README.md](src/main/java/com/fruit/entity/README.md) |
| `dto/` | 请求参数对象 | [dto/README.md](src/main/java/com/fruit/dto/README.md) |
| `vo/` | 响应数据对象 | [vo/README.md](src/main/java/com/fruit/vo/README.md) |
| `config/` | 配置类 | [config/README.md](src/main/java/com/fruit/config/README.md) |
| `common/` | 公共模块 | [common/README.md](src/main/java/com/fruit/common/README.md) |

## 🎯 功能模块

### 消费者功能 (/user, /product, /category, /cart, /order, /address, /home)
- ✅ 用户注册/登录
- ✅ 商品浏览/搜索
- ✅ 商品分类
- ✅ 购物车管理
- ✅ 订单管理（下单、支付、取消、确认收货）
- ✅ 收货地址管理
- ✅ 首页轮播图/热销/新品

### 商家功能 (/merchant/*)
- ✅ 商家注册/登录（需审核）
- ✅ 店铺信息管理
- ✅ 商品管理（增删改查、上下架）
- ✅ 库存管理
- ✅ 订单管理（发货、查看）

### 管理员功能 (/admin/*)
- ✅ 管理员登录
- ✅ 用户管理（审核、禁用）
- ✅ 商品管理（审核上下架）
- ✅ 分类管理
- ✅ 轮播图管理
- ✅ 订单管理
- ✅ 店铺审核管理

## 🚀 快速开始

### 环境要求

**后端环境**：
- JDK 1.8+
- Maven 3.6+
- MySQL 8.0+
- Redis 3.2+ （可选，用于缓存和 Token 存储）

**前端环境**：
- Node.js 16+ / 18+ / 20+
- npm 8+ 或 pnpm 7+

### 1. 数据库初始化

```bash
# 登录 MySQL
mysql -u root -p

# 创建数据库并导入初始化脚本
CREATE DATABASE fruit_ecommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE fruit_ecommerce;
SOURCE /path/to/sql/init.sql;
```

**数据库信息**：
- 数据库名：`fruit_ecommerce`
- 默认端口：`3306`
- 编码：`UTF-8 (utf8mb4)`

### 2. 后端配置与启动

#### 2.1 修改配置文件

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fruit_ecommerce?useSSL=false&characterEncoding=utf8&serverTimezone=Asia/Shanghai
    username: root          # 修改为你的数据库用户名
    password: 123456        # 修改为你的数据库密码
    
  redis:
    host: localhost
    port: 6379
    password:              # 如果 Redis 有密码，填写在这里
    database: 0

# JWT 配置（生产环境建议修改密钥）
jwt:
  secret: your-secret-key-here
  expiration: 604800      # Token 有效期（秒），默认 7 天
```

#### 2.2 启动后端

**方式一：使用 Maven 命令**
```bash
# 编译项目
mvn clean package -DskipTests

# 启动项目
mvn spring-boot:run
```

**方式二：使用 IDE 运行**
- 直接运行 `com.fruit.FruitApplication` 主类

**验证启动成功**：
- 控制台输出：`Started FruitApplication in X seconds`
- 访问：http://localhost:8080/doc.html （Knife4j API 文档）

### 3. 前端配置与启动

#### 3.1 安装依赖

```bash
cd frontend
npm install
# 或使用 pnpm（推荐，更快）
pnpm install
```

#### 3.2 配置 API 基础路径（可选）

如果后端地址不是 `http://localhost:8080`，需要修改 `frontend/src/api/request.ts`：

```typescript
const request = axios.create({
  baseURL: 'http://localhost:8080/api',  // 修改为实际后端地址
  timeout: 10000
})
```

#### 3.3 启动前端开发服务器

```bash
npm run dev
# 或
pnpm dev
```

**验证启动成功**：
- 控制台输出：`Local: http://localhost:3000/`
- 浏览器访问：http://localhost:3000

### 4. 访问系统

| 端口 | 服务 | 地址 | 说明 |
|------|------|------|------|
| 8080 | 后端 API | http://localhost:8080 | Spring Boot 应用 |
| 8080 | API 文档 | http://localhost:8080/doc.html | Knife4j 接口文档 |
| 3000 | 前端应用 | http://localhost:3000 | Vue 3 前端页面 |
| 3000 | 消费者端 | http://localhost:3000/ | 商城首页 |
| 3000 | 商家端 | http://localhost:3000/merchant/login | 商家后台 |
| 3000 | 管理员端 | http://localhost:3000/admin/login | 管理后台 |

### 5. 默认测试账号

| 角色 | 用户名 | 密码 | 说明 |
|------|--------|------|------|
| 消费者 | testuser | test123 | 普通购物用户 |
| 管理员 | admin | admin123 | 平台管理员（需先在数据库创建） |

> **提示**：首次使用建议先注册消费者账号进行体验。

## 📚 API 接口说明

### 消费者接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | POST /user/register | 用户注册 |
| 用户 | POST /user/login | 用户登录 |
| 商品 | GET /product/list | 商品列表 |
| 购物车 | POST /cart/add | 添加购物车 |
| 订单 | POST /order/create | 创建订单 |
| 地址 | POST /address/add | 添加地址 |
| 首页 | GET /home/index | 首页数据 |

### 商家接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 用户 | POST /merchant/register | 商家注册 |
| 用户 | POST /merchant/login | 商家登录 |
| 商品 | POST /merchant/product/add | 添加商品 |
| 商品 | PUT /merchant/product/onSale/{id} | 商品上架 |
| 订单 | POST /merchant/order/deliver | 订单发货 |
| 店铺 | GET /merchant/shop/info | 店铺信息 |

### 管理员接口

| 模块 | 接口 | 说明 |
|------|------|------|
| 登录 | POST /admin/login | 管理员登录 |
| 用户 | PUT /admin/user/approve/{id} | 用户审核 |
| 商品 | PUT /admin/product/approve/{id} | 商品审核 |
| 分类 | POST /admin/category/add | 添加分类 |
| 轮播图 | POST /admin/banner/add | 添加轮播图 |
| 店铺 | PUT /admin/shop/approve/{id} | 店铺审核 |

## 🔐 认证说明

系统使用 **JWT Token** 进行无状态身份认证：

### 认证流程

1. **用户登录**：发送用户名和密码到登录接口
2. **获取 Token**：登录成功后，后端返回包含 JWT Token 的响应
3. **携带 Token**：后续所有请求在 HTTP Header 中携带 Token：
   ```
   Authorization: Bearer <your-jwt-token>
   ```
4. **Token 验证**：后端拦截器自动验证 Token 有效性
5. **Token 续期**：Token 存储在 Redis 中，有效期为 7 天

### API 响应格式

所有 API 返回统一的 JSON 格式：

```json
{
  "code": 200,              // 响应码（200=成功，其他=失败）
  "message": "操作成功",     // 提示信息
  "data": {                // 实际数据
    "userId": 1,
    "username": "testuser",
    "token": "eyJhbGc..."
  },
  "timestamp": 1706756800000,  // 时间戳
  "success": true          // 是否成功（便于前端判断）
}
```

### 前端 Token 处理

前端使用 Axios 拦截器自动处理：
- **请求拦截器**：自动从 localStorage 读取 Token 并添加到 Header
- **响应拦截器**：自动解析 `response.data`，统一错误处理

### 角色与权限

| 角色 | 英文标识 | 权限范围 |
|------|---------|---------|
| 消费者 | `CONSUMER` | 浏览商品、购物、下单、管理个人信息 |
| 商家 | `MERCHANT` | 管理店铺、发布商品、处理订单 |
| 管理员 | `ADMIN` | 管理用户、审核商品/店铺、系统配置 |

> **注意**：跨角色访问会被拦截器拦截并返回 403 错误。

## 🎨 数据库设计

### 核心表结构

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| `user` | 用户表（消费者/商家/管理员） | id, username, password, role, status, avatar |
| `merchant_shop` | 商家店铺表 | id, merchant_id, shop_name, description, status |
| `category` | 商品分类表 | id, name, parent_id, level, sort |
| `product` | 商品表 | id, merchant_id, category_id, name, price, stock, main_image |
| `cart` | 购物车表 | id, user_id, product_id, quantity |
| `address` | 收货地址表 | id, user_id, receiver, phone, province, city, district, detail |
| `order` | 订单表 | id, user_id, order_no, total_amount, status, pay_time |
| `order_item` | 订单明细表 | id, order_id, product_id, quantity, price |
| `product_review` | 商品评价表 | id, product_id, user_id, rating, content |
| `banner` | 轮播图表 | id, image_url, link_url, sort, status |

### ER 图关系

```
┌──────────┐         ┌──────────────┐         ┌──────────┐
│   User   │────────>│ Merchant_Shop│────────>│  Product │
└──────────┘   1:1   └──────────────┘   1:n   └──────────┘
     │                                              │
     │ 1:n                                          │ n:1
     ▼                                              ▼
┌──────────┐                                  ┌──────────┐
│   Cart   │                                  │ Category │
└──────────┘                                  └──────────┘
     │
     │ 1:n
     ▼
┌──────────┐         ┌──────────────┐
│  Order   │────────>│  Order_Item  │
└──────────┘   1:n   └──────────────┘
     │
     │ 1:n
     ▼
┌──────────┐
│ Address  │
└──────────┘
```

### 表详细说明

查看 SQL 初始化脚本：[sql/init.sql](sql/init.sql)

### 索引设计

- **主键索引**：所有表的 `id` 字段（自增主键）
- **外键索引**：`user_id`, `product_id`, `order_id` 等关联字段
- **业务索引**：`order_no`（订单号）、`username`（用户名）
- **复合索引**：`(user_id, status)`（订单状态查询）

### 数据库优化建议

1. **读写分离**：配置主从复制，读操作走从库
2. **分表分库**：订单表按月分表，商品表按分类分表
3. **缓存策略**：热点商品数据缓存到 Redis，设置合理过期时间
4. **慢查询优化**：通过 Druid 监控慢 SQL，添加合适索引

## 📌 注意事项

### 安全建议

1. **JWT 密钥**：生产环境必须修改 `application.yml` 中的 `jwt.secret` 为强随机字符串
2. **HTTPS**：生产环境建议启用 HTTPS，防止 Token 被窃取
3. **密码加密**：当前使用 MD5 + 盐值，生产环境建议升级为 BCrypt 或 Argon2
4. **Redis 密码**：Redis 务必配置密码，防止未授权访问
5. **CORS 配置**：生产环境需限制允许的跨域来源，不要使用 `*`
6. **SQL 注入**：已使用 MyBatis Plus 参数化查询，避免字符串拼接 SQL

### 性能优化

1. **数据库连接池**：Druid 已配置，可在 `http://localhost:8080/druid` 查看监控（需配置用户名密码）
2. **Redis 缓存**：热点数据（商品详情、分类列表）应缓存到 Redis
3. **分页查询**：使用 MyBatis Plus 的 `Page` 对象，避免全表扫描
4. **图片优化**：商品图片建议使用 OSS 对象存储，启用 CDN 加速

### 开发建议

1. **日志规范**：使用 SLF4J + Logback，区分 debug/info/warn/error 级别
2. **异常处理**：已配置全局异常处理器，业务异常使用 `BusinessException`
3. **代码规范**：建议使用阿里巴巴 Java 开发手册规范，集成 CheckStyle 插件
4. **Git 提交**：提交信息格式建议：`feat: 添加xxx功能` / `fix: 修复xxx问题`

### 部署建议

1. **打包部署**：
   ```bash
   # 后端打包
   mvn clean package -DskipTests
   # 生成 target/fruit-fresh-ecommerce.jar
   
   # 前端打包
   cd frontend
   npm run build
   # 生成 dist/ 目录
   ```

2. **运行环境**：
   - 后端：使用 `java -jar` 或 Docker 容器化部署
   - 前端：使用 Nginx 托管静态文件，配置反向代理到后端

3. **Docker 部署**（可选）：
   ```bash
   # 构建 Spring Boot 镜像
   docker build -t fruit-ecommerce-backend .
   
   # 运行容器
   docker run -d -p 8080:8080 \
     -e MYSQL_HOST=host.docker.internal \
     -e REDIS_HOST=host.docker.internal \
     fruit-ecommerce-backend
   ```

### 常见问题

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 后端启动失败 | 数据库连接失败 | 检查 MySQL 是否启动，`application.yml` 配置是否正确 |
| 前端无法登录 | Token 验证失败 | 清除浏览器 localStorage，检查后端是否返回 Token |
| API 404 错误 | 路由配置错误 | 确认 Controller 的 `@RequestMapping` 路径是否正确 |
| Redis 连接失败 | Redis 未启动 | 启动 Redis 服务，或在 `application.yml` 中注释 Redis 配置 |
| 跨域问题 | CORS 未配置 | 检查 `WebMvcConfig` 的 CORS 配置，确保允许前端地址 |

### 项目扩展

如需扩展功能，建议阅读以下模块文档：
- [Controller 层说明](src/main/java/com/fruit/controller/README.md) - 如何添加新接口
- [Service 层说明](src/main/java/com/fruit/service/README.md) - 如何编写业务逻辑
- [前端架构说明](frontend/README.md) - 如何添加新页面和路由

## 📄 License

MIT License

Copyright (c) 2026 Fruit Fresh E-commerce

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED.

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建特性分支：`git checkout -b feature/AmazingFeature`
3. 提交更改：`git commit -m 'feat: Add some AmazingFeature'`
4. 推送到分支：`git push origin feature/AmazingFeature`
5. 提交 Pull Request

## 📮 联系方式

- 项目仓库：GitHub / Gitee
- 问题反馈：提交 Issue
- 邮箱：your-email@example.com

## 👨‍💻 作者

毕业设计项目 - 水果生鲜电商系统

---

**感谢使用本项目！如果对你有帮助，欢迎 Star ⭐️**
