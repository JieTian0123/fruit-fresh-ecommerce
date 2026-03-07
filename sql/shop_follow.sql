-- ================================
-- 水果生鲜电商系统 - 关注店铺功能数据库脚本
-- ================================

USE `fruit_ecommerce`;

-- ================================
-- 关注店铺表
-- ================================
DROP TABLE IF EXISTS `shop_follow`;
CREATE TABLE `shop_follow` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `shop_id` BIGINT(20) NOT NULL COMMENT '店铺ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='关注店铺表';

-- ================================
-- 用户表添加关注店铺数字段
-- ================================
ALTER TABLE `user` ADD COLUMN `follow_shop_count` INT(11) NOT NULL DEFAULT 0 COMMENT '关注店铺数量' AFTER `total_consumption`;
