-- ================================
-- 水果生鲜电商系统 - 会员系统数据库脚本
-- ================================

USE `fruit_ecommerce`;

-- ================================
-- 会员等级表
-- ================================
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '会员等级ID',
  `level_name` VARCHAR(50) NOT NULL COMMENT '等级名称',
  `level_code` VARCHAR(20) NOT NULL COMMENT '等级代码',
  `required_points` INT(11) NOT NULL DEFAULT 0 COMMENT '所需积分',
  `discount_rate` DECIMAL(3,2) NOT NULL DEFAULT 1.00 COMMENT '折扣率 0.95表示95折',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '等级图标',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '等级颜色',
  `benefits` TEXT COMMENT '会员权益说明(JSON格式)',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  KEY `idx_required_points` (`required_points`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员等级表';

-- 插入默认会员等级数据
INSERT INTO `member_level` (`level_name`, `level_code`, `required_points`, `discount_rate`, `color`, `benefits`, `sort`) VALUES 
('普通会员', 'NORMAL', 0, 1.00, '#999999', '["享受平台基础服务", "参与平台活动"]', 1),
('银卡会员', 'SILVER', 500, 0.98, '#C0C0C0', '["98折优惠", "专属客服", "生日礼包", "优先发货"]', 2),
('金卡会员', 'GOLD', 2000, 0.95, '#FFD700', '["95折优惠", "专属客服", "生日礼包", "优先发货", "免运费券", "专享活动"]', 3),
('钻石会员', 'DIAMOND', 5000, 0.92, '#B9F2FF', '["92折优惠", "VIP客服", "生日礼包", "优先发货", "免运费券", "专享活动", "新品试用", "积分翻倍"]', 4);

-- ================================
-- 优惠券表
-- ================================
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `title` VARCHAR(100) NOT NULL COMMENT '优惠券标题',
  `coupon_type` INT(1) NOT NULL COMMENT '优惠券类型 1-满减券 2-折扣券 3-无门槛券',
  `discount_amount` DECIMAL(10,2) DEFAULT NULL COMMENT '优惠金额(满减券、无门槛券)',
  `discount_rate` DECIMAL(3,2) DEFAULT NULL COMMENT '折扣率(折扣券) 0.9表示9折',
  `minimum_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额',
  `maximum_discount` DECIMAL(10,2) DEFAULT NULL COMMENT '最高优惠金额(折扣券)',
  `total_quantity` INT(11) NOT NULL DEFAULT 0 COMMENT '发行总量',
  `received_quantity` INT(11) NOT NULL DEFAULT 0 COMMENT '已领取数量',
  `used_quantity` INT(11) NOT NULL DEFAULT 0 COMMENT '已使用数量',
  `per_user_limit` INT(11) DEFAULT 1 COMMENT '每人限领数量',
  `valid_from` DATETIME NOT NULL COMMENT '有效期开始时间',
  `valid_until` DATETIME NOT NULL COMMENT '有效期结束时间',
  `valid_days` INT(11) DEFAULT NULL COMMENT '领取后有效天数(NULL表示使用固定时间)',
  `applicable_categories` VARCHAR(500) DEFAULT NULL COMMENT '适用分类ID(逗号分隔,NULL表示全场)',
  `applicable_products` VARCHAR(500) DEFAULT NULL COMMENT '适用商品ID(逗号分隔,NULL表示全场)',
  `member_levels` VARCHAR(100) DEFAULT NULL COMMENT '适用会员等级(逗号分隔,NULL表示全部)',
  `description` TEXT COMMENT '使用说明',
  `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用 2-已结束',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_type` (`coupon_type`),
  KEY `idx_valid_time` (`valid_from`, `valid_until`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 插入默认优惠券数据
INSERT INTO `coupon` (`title`, `coupon_type`, `discount_amount`, `minimum_amount`, `total_quantity`, `per_user_limit`, `valid_from`, `valid_until`, `description`) VALUES 
('新用户专享券', 3, 10.00, 0.00, 10000, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), '新用户注册即可领取，全场通用'),
('满100减20优惠券', 1, 20.00, 100.00, 5000, 3, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), '全场通用，满100元可用'),
('9折优惠券', 2, NULL, 50.00, 3000, 2, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY), '全场通用，单笔订单最高优惠50元');

UPDATE `coupon` SET `discount_rate` = 0.90, `maximum_discount` = 50.00 WHERE `coupon_type` = 2;

-- ================================
-- 用户优惠券表
-- ================================
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `coupon_id` BIGINT(20) NOT NULL COMMENT '优惠券ID',
  `status` INT(1) NOT NULL DEFAULT 0 COMMENT '状态 0-未使用 1-已使用 2-已过期',
  `receive_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` DATETIME DEFAULT NULL COMMENT '使用时间',
  `order_no` VARCHAR(64) DEFAULT NULL COMMENT '订单号',
  `valid_from` DATETIME NOT NULL COMMENT '有效期开始时间',
  `valid_until` DATETIME NOT NULL COMMENT '有效期结束时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`),
  KEY `idx_valid_time` (`valid_from`, `valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ================================
-- 用户积分记录表
-- ================================
DROP TABLE IF EXISTS `user_points_log`;
CREATE TABLE `user_points_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `points` INT(11) NOT NULL COMMENT '积分变动(正数为增加,负数为减少)',
  `source_type` INT(1) NOT NULL COMMENT '来源类型 1-注册 2-签到 3-消费 4-评价 5-兑换 6-过期 7-管理员调整',
  `source_id` VARCHAR(64) DEFAULT NULL COMMENT '来源ID(如订单号、兑换记录ID)',
  `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
  `balance_after` INT(11) NOT NULL COMMENT '变动后积分余额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分记录表';

-- ================================
-- 用户签到表
-- ================================
DROP TABLE IF EXISTS `user_sign_in`;
CREATE TABLE `user_sign_in` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '签到记录ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `sign_date` DATE NOT NULL COMMENT '签到日期',
  `continuous_days` INT(11) NOT NULL DEFAULT 1 COMMENT '连续签到天数',
  `points_earned` INT(11) NOT NULL DEFAULT 0 COMMENT '获得积分',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `sign_date`),
  KEY `idx_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户签到表';

-- ================================
-- 修改user表 - 添加会员相关字段
-- ================================
ALTER TABLE `user` ADD COLUMN `member_level_id` BIGINT(20) DEFAULT NULL COMMENT '会员等级ID' AFTER `avatar`;
ALTER TABLE `user` ADD COLUMN `points` INT(11) NOT NULL DEFAULT 0 COMMENT '用户积分' AFTER `member_level_id`;
ALTER TABLE `user` ADD COLUMN `total_consumption` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '累计消费金额' AFTER `points`;

ALTER TABLE `user` ADD KEY `idx_member_level_id` (`member_level_id`);

-- 将现有用户设置为普通会员
UPDATE `user` SET `member_level_id` = 1 WHERE `user_type` = 0 AND `member_level_id` IS NULL;

-- ================================
-- 修改order表 - 添加优惠券字段
-- ================================
ALTER TABLE `order` ADD COLUMN `coupon_id` BIGINT(20) DEFAULT NULL COMMENT '使用的优惠券ID' AFTER `total_amount`;
ALTER TABLE `order` ADD COLUMN `coupon_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠券优惠金额' AFTER `coupon_id`;
ALTER TABLE `order` ADD COLUMN `points_earned` INT(11) DEFAULT 0 COMMENT '订单获得积分' AFTER `coupon_amount`;

ALTER TABLE `order` ADD KEY `idx_coupon_id` (`coupon_id`);
