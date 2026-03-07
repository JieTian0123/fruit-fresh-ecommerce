# Entity Module (实体类模块)

## 简介
`entity` 包存放与数据库表一一对应的 POJO 类（Persistent Object）。

## 技术栈
本项目使用 **MyBatis-Plus** 作为 ORM 框架，因此实体类通常会使用以下注解：
- `@TableName("t_user")`: 指定映射的数据库表名。
- `@TableId`: 指定主键策略。
- `@TableField`: 指定普通字段映射。

## 核心作用
它是数据持久层的核心模型，承载了数据库中的原始数据。通常不建议直接将 Entity 返回给前端，而是转化为 VO (View Object) 后返回，以保护数据库结构隐私。
