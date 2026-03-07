# 水果生鲜电商系统 - 修复记录

本文档记录了对项目进行的安全修复和代码改进。

---

## 1. 安全修复

### 1.1 密码加密改用 BCrypt

**问题**: 原系统使用 MD5 + 固定盐值加密密码，存在安全隐患。

**修改文件**: `src/main/java/com/fruit/utils/PasswordUtils.java`

**修复内容**:
- 新用户注册使用 BCrypt 加密（强度因子 10）
- 保持向后兼容：系统可验证旧 MD5 格式的密码
- 验证时自动识别密码格式（BCrypt 以 `$2a$` 开头）

**添加依赖** (`pom.xml`):
```xml
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
</dependency>
```

### 1.2 移除硬编码敏感信息

**问题**: 数据库密码、Redis 密码、JWT 密钥等敏感信息直接硬编码在配置文件中。

**修改文件**: 
- `src/main/resources/application.yml` - 重写为主配置文件
- `src/main/resources/application-dev.yml` - 新建开发环境配置
- `src/main/resources/application-prod.yml` - 新建生产环境配置

**修复内容**:
- 所有敏感配置支持环境变量覆盖
- 开发环境使用默认值，生产环境必须通过环境变量配置

**支持的环境变量**:
| 环境变量 | 说明 | 开发默认值 |
|---------|------|-----------|
| `DB_URL` | 数据库连接URL | jdbc:mysql://localhost:3306/fruit_ecommerce... |
| `DB_USERNAME` | 数据库用户名 | root |
| `DB_PASSWORD` | 数据库密码 | 123456 |
| `REDIS_HOST` | Redis主机 | localhost |
| `REDIS_PORT` | Redis端口 | 6379 |
| `REDIS_PASSWORD` | Redis密码 | (空) |
| `JWT_SECRET` | JWT签名密钥 | FruitEcommerceSecretKey2024DefaultDevOnly |
| `JWT_EXPIRATION` | Token过期时间(毫秒) | 604800000 (7天) |
| `UPLOAD_PATH` | 文件上传路径 | ./uploads |

**生产环境启动示例**:
```bash
# Windows
set DB_PASSWORD=your_secure_password
set REDIS_PASSWORD=your_redis_password
set JWT_SECRET=your_production_jwt_secret_key_at_least_64_chars
java -jar fruit-fresh-ecommerce.jar --spring.profiles.active=prod

# Linux/Mac
export DB_PASSWORD=your_secure_password
export REDIS_PASSWORD=your_redis_password
export JWT_SECRET=your_production_jwt_secret_key_at_least_64_chars
java -jar fruit-fresh-ecommerce.jar --spring.profiles.active=prod
```

### 1.3 移除调试语句

**问题**: `OrderController.java` 遗留 `System.out.println` 调试语句。

**修改文件**: `src/main/java/com/fruit/controller/consumer/OrderController.java`

**修复内容**: 删除第 37、40-41 行的调试输出语句。

---

## 2. 依赖升级 (CVE 修复)

**修改文件**: `pom.xml`

| 依赖 | 原版本 | 新版本 | 修复的 CVE |
|------|--------|--------|-----------|
| mybatis-plus | 3.5.5 | 3.5.6 | CVE-2024-35548 |
| hutool | 5.8.25 | 5.8.40 | CVE-2025-56769 |
| jjwt | 0.9.1 | 0.11.5 | 多个安全漏洞 |

### jjwt 升级说明

jjwt 0.11.x 采用模块化设计，依赖配置变更：

```xml
<!-- 旧版本 (0.9.1) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt</artifactId>
    <version>0.9.1</version>
</dependency>

<!-- 新版本 (0.11.5) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**同步修改**: `src/main/java/com/fruit/utils/JwtUtils.java`
- 使用 `Keys.hmacShaKeyFor()` 创建密钥
- 使用 `Jwts.parserBuilder()` 替代 `Jwts.parser()`
- 密钥自动填充到 64 字节以满足 HS512 要求

---

## 3. 前端代码改进

### 3.1 TypeScript 类型修复

**修改文件**: 
- `frontend/src/types/index.ts` - 添加新类型定义
- `frontend/src/api/user.ts` - 移除 `any` 类型

**新增类型**:
```typescript
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

export interface MerchantLoginResponse {
  token: string
  userInfo: UserInfo
  shopId?: number
  shopName?: string
}

export interface AdminLoginResponse {
  token: string
  userInfo: UserInfo
}

export interface Banner {
  id: number
  title: string
  imageUrl: string
  linkUrl: string
  sort: number
  status: number
  createTime?: string
}
```

### 3.2 参数类型修复

**修改文件**: `frontend/src/views/consumer/HomeView.vue`

将 `params: any` 改为 `params: ProductQuery`。

### 3.3 错误处理改进

**修改文件**: `frontend/src/stores/cart.ts`

原先的 catch 块只是静默刷新列表，现在添加了用户提示：
- `updateQuantity`: 失败时显示"更新数量失败，请重试"
- `removeItem`: 失败时显示"删除失败，请重试"
- `toggleSelect`: 失败时显示"操作失败，请重试"
- `toggleSelectAll`: 失败时显示"操作失败，请重试"
- `clearAll`: 失败时显示"清空失败，请重试"

---

## 4. 待办事项 (TODO)

以下是已识别但未在本次修复中处理的问题：

### 4.1 后端 Banner API 缺失

数据库已有 `banner` 表，但后端缺少对应的 REST API。

**当前状态**: 前端 `HomeView.vue` 使用硬编码轮播图数据。

**建议**: 实现以下接口：
- `GET /api/home/banners` - 获取启用的轮播图列表

### 4.2 Spring Boot 版本

当前使用 Spring Boot 2.7.18，已 EOL（End of Life），存在 29 个已知 CVE。

**当前状态**: 保持不变（毕业设计项目要求）。

**建议**: 生产环境考虑升级到 Spring Boot 3.x（需要 Java 17+）。

---

## 5. 验证清单

修复完成后，请验证以下功能：

- [ ] 用户登录（消费者/商家/管理员）
- [ ] 新用户注册（密码使用 BCrypt 加密）
- [ ] 老用户登录（MD5 密码兼容）
- [ ] 订单列表查询
- [ ] 购物车操作（添加、删除、修改数量）
- [ ] JWT Token 生成和验证
- [ ] 项目编译通过：`mvn clean package -DskipTests`

---

## 修改文件清单

| 文件路径 | 修改类型 |
|---------|---------|
| `pom.xml` | 修改（依赖升级 + 添加依赖） |
| `src/main/resources/application.yml` | 重写 |
| `src/main/resources/application-dev.yml` | 新建 |
| `src/main/resources/application-prod.yml` | 新建 |
| `src/main/java/com/fruit/utils/PasswordUtils.java` | 重写（BCrypt） |
| `src/main/java/com/fruit/utils/JwtUtils.java` | 修改（适配 jjwt 0.11.x） |
| `src/main/java/com/fruit/controller/consumer/OrderController.java` | 修改（删除调试语句） |
| `frontend/src/types/index.ts` | 修改（添加类型） |
| `frontend/src/api/user.ts` | 修改（类型修复） |
| `frontend/src/views/consumer/HomeView.vue` | 修改（类型 + 注释） |
| `frontend/src/stores/cart.ts` | 修改（错误处理） |
| `FIXES.md` | 新建（本文档） |
