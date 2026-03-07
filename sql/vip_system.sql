-- =====================================================
-- VIP会员系统 - 数据库建表脚本
-- 包含：用户表VIP字段扩展、VIP套餐表、VIP订单表
-- =====================================================

-- 1. 用户表新增VIP相关字段
ALTER TABLE `user` ADD COLUMN `is_vip` TINYINT NOT NULL DEFAULT 0 COMMENT 'VIP状态 0-非VIP 1-VIP' AFTER `total_consumption`;
ALTER TABLE `user` ADD COLUMN `vip_expire_time` DATETIME DEFAULT NULL COMMENT 'VIP到期时间' AFTER `is_vip`;
ALTER TABLE `user` ADD COLUMN `vip_order_count` INT NOT NULL DEFAULT 0 COMMENT '已完成订单数(缓存)' AFTER `vip_expire_time`;

-- 2. VIP套餐表
CREATE TABLE IF NOT EXISTS `vip_plan` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `name` VARCHAR(50) NOT NULL COMMENT '套餐名称',
    `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
    `duration_days` INT NOT NULL COMMENT '有效天数',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '套餐描述',
    `benefits` TEXT DEFAULT NULL COMMENT '权益说明(JSON)',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-否 1-是'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP套餐表';

-- 3. VIP订单表
CREATE TABLE IF NOT EXISTS `vip_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `plan_id` BIGINT DEFAULT NULL COMMENT 'VIP套餐ID(购买方式)',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单号',
    `amount` DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT '支付金额',
    `source` TINYINT NOT NULL DEFAULT 1 COMMENT '来源 1-购买 2-订单达标自动升级',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待付款 1-已付款',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `vip_start_time` DATETIME DEFAULT NULL COMMENT 'VIP开始时间',
    `vip_end_time` DATETIME DEFAULT NULL COMMENT 'VIP结束时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-否 1-是',
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VIP订单表';

-- 4. 插入默认VIP套餐数据
INSERT INTO `vip_plan` (`name`, `price`, `duration_days`, `description`, `sort`, `status`) VALUES
('月度VIP', 9.90, 30, '月度VIP会员，享受每周专属优惠券、签到积分双倍等特权', 1, 1),
('季度VIP', 25.00, 90, '季度VIP会员，享受每周专属优惠券、签到积分双倍等特权，更划算', 2, 1),
('年度VIP', 88.00, 365, '年度VIP会员，享受每周专属优惠券、签到积分双倍等特权，超值之选', 3, 1);

-- 5. VIP自动升级阈值说明：
-- 用户完成10笔订单(status=3, 已确认收货)后自动升级为VIP(365天)
-- 该逻辑在Java业务层实现，不需要数据库触发器
