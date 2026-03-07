-- ================================
-- 水果新鲜度与溯源功能 - 数据库迁移
-- ================================

USE `fruit_ecommerce`;

-- 1. product 表添加新鲜度相关字段
ALTER TABLE `product`
  ADD COLUMN `shelf_life_days` INT(11) DEFAULT NULL COMMENT '保质期天数' AFTER `weight`,
  ADD COLUMN `production_date` DATE DEFAULT NULL COMMENT '生产/采摘日期' AFTER `shelf_life_days`,
  ADD COLUMN `storage_condition` VARCHAR(100) DEFAULT NULL COMMENT '存储条件（如：0-4℃冷藏）' AFTER `production_date`,
  ADD COLUMN `quality_grade` VARCHAR(10) DEFAULT NULL COMMENT '品质等级（A/B/C）' AFTER `storage_condition`;

-- 2. 新建商品溯源表
DROP TABLE IF EXISTS `product_traceability`;
CREATE TABLE `product_traceability` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '溯源记录ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `node_type` INT(1) NOT NULL COMMENT '节点类型 1-采摘 2-质检 3-入库 4-出库 5-配送',
  `node_name` VARCHAR(50) NOT NULL COMMENT '节点名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `location` VARCHAR(200) DEFAULT NULL COMMENT '地点',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
  `temperature` DECIMAL(5,1) DEFAULT NULL COMMENT '温度(℃)',
  `humidity` DECIMAL(5,1) DEFAULT NULL COMMENT '湿度(%)',
  `image_url` VARCHAR(255) DEFAULT NULL COMMENT '图片',
  `occurred_time` DATETIME NOT NULL COMMENT '发生时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_node_type` (`node_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品溯源表';

-- 3. 为现有商品添加示例新鲜度数据（方便演示）
UPDATE `product` SET
  `shelf_life_days` = 7,
  `production_date` = CURDATE() - INTERVAL 1 DAY,
  `storage_condition` = '0-4℃冷藏',
  `quality_grade` = 'A'
WHERE `category_id` = 1 AND `deleted` = 0;

UPDATE `product` SET
  `shelf_life_days` = 5,
  `production_date` = CURDATE() - INTERVAL 2 DAY,
  `storage_condition` = '0-4℃冷藏',
  `quality_grade` = 'A'
WHERE `category_id` = 2 AND `deleted` = 0;

UPDATE `product` SET
  `shelf_life_days` = 14,
  `production_date` = CURDATE(),
  `storage_condition` = '阴凉干燥处保存',
  `quality_grade` = 'B'
WHERE `category_id` NOT IN (1, 2) AND `deleted` = 0 AND `shelf_life_days` IS NULL;
