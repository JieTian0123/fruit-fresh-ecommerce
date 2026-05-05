-- ================================
-- 优惠券系统升级 - VIP免费领取功能
-- ================================

USE `fruit_ecommerce`;

-- 为优惠券表添加积分兑换价格字段（如果不存在）
ALTER TABLE `coupon` ADD COLUMN IF NOT EXISTS `points_price` INT(11) DEFAULT NULL COMMENT '积分兑换价格(NULL表示不可兑换)' AFTER `member_levels`;

-- 为优惠券表添加VIP免费领取字段
ALTER TABLE `coupon` ADD COLUMN IF NOT EXISTS `vip_free_receive` INT(1) DEFAULT 0 COMMENT 'VIP会员是否可免费领取 0-否 1-是' AFTER `points_price`;

-- 添加索引
ALTER TABLE `coupon` ADD KEY IF NOT EXISTS `idx_vip_free_receive` (`vip_free_receive`);

-- 更新现有优惠券，设置默认值
UPDATE `coupon` SET `vip_free_receive` = 0 WHERE `vip_free_receive` IS NULL;
