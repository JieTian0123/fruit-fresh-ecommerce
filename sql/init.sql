-- ================================
-- 水果生鲜电商系统 - 数据库初始化脚本
-- ================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `fruit_ecommerce` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `fruit_ecommerce`;

-- ================================
-- 用户表
-- ================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(100) NOT NULL COMMENT '密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `gender` INT(1) DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
  `user_type` INT(1) NOT NULL DEFAULT 0 COMMENT '用户类型 0-消费者 1-商家 2-管理员',
  `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用 2-待审核',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 插入默认管理员账户 (密码: admin123)
INSERT INTO `user` (`username`, `password`, `nickname`, `user_type`, `status`) 
VALUES ('admin', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 2, 1);

-- ================================
-- 商品分类表
-- ================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT(20) DEFAULT 0 COMMENT '父分类ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(255) DEFAULT NULL COMMENT '分类图标',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 插入默认分类数据
INSERT INTO `category` (`name`, `sort`) VALUES 
('新鲜水果', 1),
('时令蔬菜', 2),
('肉禽蛋品', 3),
('海鲜水产', 4),
('粮油调味', 5),
('乳品烘焙', 6);

-- ================================
-- 商家店铺表
-- ================================
DROP TABLE IF EXISTS `merchant_shop`;
CREATE TABLE `merchant_shop` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
  `merchant_id` BIGINT(20) NOT NULL COMMENT '商家用户ID',
  `shop_name` VARCHAR(100) NOT NULL COMMENT '店铺名称',
  `logo` VARCHAR(255) DEFAULT NULL COMMENT '店铺Logo',
  `description` TEXT COMMENT '店铺描述',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
  `business_license` VARCHAR(255) DEFAULT NULL COMMENT '营业执照',
  `status` INT(1) NOT NULL DEFAULT 2 COMMENT '状态 0-禁用 1-启用 2-待审核',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商家店铺表';

-- ================================
-- 商品表
-- ================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` BIGINT(20) NOT NULL COMMENT '商家ID',
  `shop_id` BIGINT(20) DEFAULT NULL COMMENT '店铺ID',
  `category_id` BIGINT(20) NOT NULL COMMENT '分类ID',
  `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `subtitle` VARCHAR(255) DEFAULT NULL COMMENT '副标题',
  `main_image` VARCHAR(255) DEFAULT NULL COMMENT '主图',
  `sub_images` TEXT COMMENT '子图（JSON数组）',
  `detail` TEXT COMMENT '商品详情',
  `price` DECIMAL(10,2) NOT NULL COMMENT '价格',
  `original_price` DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
  `stock` INT(11) NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` INT(11) DEFAULT 0 COMMENT '销量',
  `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位',
  `weight` DECIMAL(10,2) DEFAULT NULL COMMENT '重量(kg)',
  `status` INT(1) NOT NULL DEFAULT 2 COMMENT '状态 0-下架 1-上架 2-待审核',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- ================================
-- 购物车表
-- ================================
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `merchant_id` BIGINT(20) NOT NULL COMMENT '商家ID',
  `quantity` INT(11) NOT NULL DEFAULT 1 COMMENT '数量',
  `selected` INT(1) DEFAULT 1 COMMENT '是否选中 0-否 1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- ================================
-- 用户收货地址表
-- ================================
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `is_default` INT(1) DEFAULT 0 COMMENT '是否默认 0-否 1-是',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ================================
-- 订单表
-- ================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `merchant_id` BIGINT(20) NOT NULL COMMENT '商家ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货地址',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '商品总金额',
  `freight_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '运费',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `pay_type` INT(1) DEFAULT NULL COMMENT '支付方式 1-微信 2-支付宝',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `status` INT(1) NOT NULL DEFAULT 0 COMMENT '订单状态 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消',
  `delivery_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
  `delivery_no` VARCHAR(50) DEFAULT NULL COMMENT '物流单号',
  `delivery_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单备注',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ================================
-- 订单明细表
-- ================================
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称',
  `product_image` VARCHAR(255) DEFAULT NULL COMMENT '商品图片',
  `price` DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  `quantity` INT(11) NOT NULL COMMENT '购买数量',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '小计金额',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ================================
-- 商品评价表
-- ================================
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `product_id` BIGINT(20) NOT NULL COMMENT '商品ID',
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `rating` INT(1) NOT NULL DEFAULT 5 COMMENT '评分 1-5星',
  `content` TEXT COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片（JSON数组）',
  `reply` TEXT COMMENT '商家回复',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `status` INT(1) DEFAULT 1 COMMENT '状态 0-隐藏 1-显示',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ================================
-- 轮播图/Banner表
-- ================================
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `image_url` VARCHAR(255) NOT NULL COMMENT '图片URL',
  `link_url` VARCHAR(255) DEFAULT NULL COMMENT '跳转链接',
  `link_type` INT(1) DEFAULT 0 COMMENT '跳转类型 0-无跳转 1-商品详情 2-分类页面 3-外链',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 插入示例轮播图数据
INSERT INTO `banner` (`title`, `image_url`, `link_type`, `sort`) VALUES
('新鲜水果 每日上新', 'https://example.com/banner1.jpg', 2, 1),
('时令蔬菜 产地直供', 'https://example.com/banner2.jpg', 2, 2),
('新人专享 立减10元', 'https://example.com/banner3.jpg', 0, 3);
