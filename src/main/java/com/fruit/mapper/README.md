# Mapper Module (DAO层)

## 简介
`mapper` 包（也称为 DAO 层）是数据访问层接口，负责与数据库进行直接交互。

## 技术栈
基于 **MyBatis-Plus**：
- 接口通常继承 `BaseMapper<T>`，无需编写 XML 即可自动获得 CRUD 能力（增删改查）。
- 复杂 SQL 可通过对应的 `xml` 文件或 `@Select`/`@Update` 注解自定义实现。

## 核心作用
将 Java 方法调用转换为 SQL 语句执行，并将数据库结果集映射回 Java 对象 (Entity)。
