SET NAMES utf8mb4;

DROP DATABASE IF EXISTS ruit_ecommerce;


CREATE DATABASE /*!32312 IF NOT EXISTS*/ `fruit_ecommerce` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `fruit_ecommerce`;
DROP TABLE IF EXISTS `address`;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) NOT NULL COMMENT '省份',
  `city` varchar(50) NOT NULL COMMENT '城市',
  `district` varchar(50) NOT NULL COMMENT '区县',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` int DEFAULT '0' COMMENT '是否默认 0-否 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户收货地址表';
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `type` int NOT NULL COMMENT '公告类型 1-系统公告 2-活动公告 3-知识科普',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text COMMENT '公告内容',
  `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `view_count` int NOT NULL DEFAULT '0' COMMENT '浏览次数',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0-草稿 1-发布 2-下架',
  `publish_time` datetime DEFAULT NULL COMMENT '发布时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='平台公告表';
DROP TABLE IF EXISTS `banner`;
CREATE TABLE `banner` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '轮播图ID',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `image_url` varchar(255) NOT NULL COMMENT '图片URL',
  `link_url` varchar(255) DEFAULT NULL COMMENT '跳转链接',
  `link_type` int DEFAULT '0' COMMENT '跳转类型 0-无跳转 1-商品详情 2-分类页面 3-外链',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='轮播图表';
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '购物车ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '数量',
  `selected` int DEFAULT '1' COMMENT '是否选中 0-否 1-是',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `title` varchar(100) NOT NULL COMMENT '优惠券标题',
  `coupon_type` int NOT NULL COMMENT '优惠券类型 1-满减券 2-折扣券 3-无门槛券',
  `discount_amount` decimal(10,2) DEFAULT NULL COMMENT '优惠金额(满减券、无门槛券)',
  `discount_rate` decimal(3,2) DEFAULT NULL COMMENT '折扣率(折扣券) 0.9表示9折',
  `minimum_amount` decimal(10,2) DEFAULT '0.00' COMMENT '最低消费金额',
  `maximum_discount` decimal(10,2) DEFAULT NULL COMMENT '最高优惠金额(折扣券)',
  `total_quantity` int NOT NULL DEFAULT '0' COMMENT '发行总量',
  `received_quantity` int NOT NULL DEFAULT '0' COMMENT '已领取数量',
  `used_quantity` int NOT NULL DEFAULT '0' COMMENT '已使用数量',
  `per_user_limit` int DEFAULT '1' COMMENT '每人限领数量',
  `valid_from` datetime NOT NULL COMMENT '有效期开始时间',
  `valid_until` datetime NOT NULL COMMENT '有效期结束时间',
  `valid_days` int DEFAULT NULL COMMENT '领取后有效天数(NULL表示使用固定时间)',
  `applicable_categories` varchar(500) DEFAULT NULL COMMENT '适用分类ID(逗号分隔,NULL表示全场)',
  `applicable_products` varchar(500) DEFAULT NULL COMMENT '适用商品ID(逗号分隔,NULL表示全场)',
  `member_levels` varchar(100) DEFAULT NULL COMMENT '适用会员等级(逗号分隔,NULL表示全部)',
  `points_price` int DEFAULT NULL COMMENT '积分兑换价格(NULL表示不可兑换)',
  `vip_free_receive` int DEFAULT '0' COMMENT 'VIP会员是否可免费领取 0-否 1-是',
  `description` text COMMENT '使用说明',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用 2-已结束',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_coupon_type` (`coupon_type`),
  KEY `idx_valid_time` (`valid_from`,`valid_until`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='优惠券表';
DROP TABLE IF EXISTS `member_level`;
CREATE TABLE `member_level` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会员等级ID',
  `level_name` varchar(50) NOT NULL COMMENT '等级名称',
  `level_code` varchar(20) NOT NULL COMMENT '等级代码',
  `required_points` int NOT NULL DEFAULT '0' COMMENT '所需积分',
  `discount_rate` decimal(3,2) NOT NULL DEFAULT '1.00' COMMENT '折扣率 0.95表示95折',
  `icon` varchar(255) DEFAULT NULL COMMENT '等级图标',
  `color` varchar(20) DEFAULT NULL COMMENT '等级颜色',
  `benefits` text COMMENT '会员权益说明(JSON格式)',
  `sort` int DEFAULT '0' COMMENT '排序',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_level_code` (`level_code`),
  KEY `idx_required_points` (`required_points`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='会员等级表';
DROP TABLE IF EXISTS `merchant_shop`;
CREATE TABLE `merchant_shop` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
  `merchant_id` bigint NOT NULL COMMENT '商家用户ID',
  `shop_name` varchar(100) NOT NULL COMMENT '店铺名称',
  `logo` varchar(255) DEFAULT NULL COMMENT '店铺Logo',
  `description` text COMMENT '店铺描述',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `address` varchar(255) DEFAULT NULL COMMENT '详细地址',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `business_license` varchar(255) DEFAULT NULL COMMENT '营业执照',
  `status` int NOT NULL DEFAULT '2' COMMENT '状态 0-禁用 1-启用 2-待审核',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商家店铺表';
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `receiver_address` varchar(255) NOT NULL COMMENT '收货地址',
  `total_amount` decimal(10,2) NOT NULL COMMENT '商品总金额',
  `coupon_id` bigint DEFAULT NULL COMMENT '使用的优惠券ID',
  `coupon_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠券优惠金额',
  `points_earned` int DEFAULT '0' COMMENT '订单获得积分',
  `freight_amount` decimal(10,2) DEFAULT '0.00' COMMENT '运费',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额',
  `pay_type` int DEFAULT NULL COMMENT '支付方式 1-微信 2-支付宝',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` int NOT NULL DEFAULT '0' COMMENT '订单状态 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消 5-退款中 6-已退款',
  `delivery_company` varchar(50) DEFAULT NULL COMMENT '物流公司',
  `delivery_no` varchar(50) DEFAULT NULL COMMENT '物流单号',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `receive_time` datetime DEFAULT NULL COMMENT '收货时间',
  `remark` varchar(255) DEFAULT NULL COMMENT '订单备注',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_status` (`status`),
  KEY `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单明细ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '商品单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '小计金额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `merchant_id` bigint NOT NULL COMMENT '商家ID',
  `shop_id` bigint DEFAULT NULL COMMENT '店铺ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `name` varchar(200) NOT NULL COMMENT '商品名称',
  `subtitle` varchar(255) DEFAULT NULL COMMENT '副标题',
  `main_image` varchar(255) DEFAULT NULL COMMENT '主图',
  `sub_images` text COMMENT '子图（JSON数组）',
  `detail` text COMMENT '商品详情',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价',
  `stock` int NOT NULL DEFAULT '0' COMMENT '库存',
  `sales` int DEFAULT '0' COMMENT '销量',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `weight` decimal(10,2) DEFAULT NULL COMMENT '重量(kg)',
  `shelf_life_days` int DEFAULT NULL COMMENT '保质期天数',
  `production_date` date DEFAULT NULL COMMENT '生产/采摘日期',
  `storage_condition` varchar(100) DEFAULT NULL COMMENT '存储条件（如：0-4℃冷藏）',
  `quality_grade` varchar(10) DEFAULT NULL COMMENT '品质等级（A/B/C）',
  `status` int NOT NULL DEFAULT '2' COMMENT '状态 0-下架 1-上架 2-待审核',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_merchant_id` (`merchant_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `rating` int NOT NULL DEFAULT '5' COMMENT '评分 1-5星',
  `content` text COMMENT '评价内容',
  `images` text COMMENT '评价图片（JSON数组）',
  `reply` text COMMENT '商家回复',
  `reply_time` datetime DEFAULT NULL COMMENT '回复时间',
  `status` int DEFAULT '1' COMMENT '状态 0-隐藏 1-显示',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';
DROP TABLE IF EXISTS `product_traceability`;
CREATE TABLE `product_traceability` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '溯源记录ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `node_type` int NOT NULL COMMENT '节点类型 1-采摘 2-质检 3-入库 4-出库 5-配送',
  `node_name` varchar(50) NOT NULL COMMENT '节点名称',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `location` varchar(200) DEFAULT NULL COMMENT '地点',
  `operator` varchar(50) DEFAULT NULL COMMENT '操作人',
  `temperature` decimal(5,1) DEFAULT NULL COMMENT '温度(℃)',
  `humidity` decimal(5,1) DEFAULT NULL COMMENT '湿度(%)',
  `image_url` varchar(255) DEFAULT NULL COMMENT '图片',
  `occurred_time` datetime NOT NULL COMMENT '发生时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_node_type` (`node_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品溯源表';
DROP TABLE IF EXISTS `shop_follow`;
CREATE TABLE `shop_follow` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `shop_id` bigint NOT NULL COMMENT '店铺ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_shop` (`user_id`,`shop_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='关注店铺表';
DROP TABLE IF EXISTS `system_notification`;
CREATE TABLE `system_notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '接收用户ID',
  `type` varchar(20) NOT NULL COMMENT '类型: order/system/promotion',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `content` text COMMENT '内容',
  `is_read` int DEFAULT '0' COMMENT '是否已读 0-未读 1-已读',
  `related_id` bigint DEFAULT NULL COMMENT '关联ID(如订单ID)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted` int DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `member_level_id` bigint DEFAULT NULL COMMENT '会员等级ID',
  `points` int NOT NULL DEFAULT '0' COMMENT '用户积分',
  `total_consumption` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '累计消费金额',
  `is_vip` tinyint NOT NULL DEFAULT '0' COMMENT 'VIP状态 0-非VIP 1-VIP',
  `vip_expire_time` datetime DEFAULT NULL COMMENT 'VIP到期时间',
  `vip_order_count` int NOT NULL DEFAULT '0' COMMENT '已完成订单数(缓存)',
  `follow_shop_count` int NOT NULL DEFAULT '0' COMMENT '关注店铺数量',
  `gender` int DEFAULT '0' COMMENT '性别 0-未知 1-男 2-女',
  `user_type` int NOT NULL DEFAULT '0' COMMENT '用户类型 0-消费者 1-商家 2-管理员',
  `status` int NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用 2-待审核',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_phone` (`phone`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_status` (`status`),
  KEY `idx_member_level_id` (`member_level_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户优惠券ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `coupon_id` bigint NOT NULL COMMENT '优惠券ID',
  `status` int NOT NULL DEFAULT '0' COMMENT '状态 0-未使用 1-已使用 2-已过期',
  `receive_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime DEFAULT NULL COMMENT '使用时间',
  `order_no` varchar(64) DEFAULT NULL COMMENT '订单号',
  `valid_from` datetime NOT NULL COMMENT '有效期开始时间',
  `valid_until` datetime NOT NULL COMMENT '有效期结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_coupon_id` (`coupon_id`),
  KEY `idx_status` (`status`),
  KEY `idx_valid_time` (`valid_from`,`valid_until`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户优惠券表';
DROP TABLE IF EXISTS `user_points_log`;
CREATE TABLE `user_points_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '积分记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `points` int NOT NULL COMMENT '积分变动(正数为增加,负数为减少)',
  `source_type` int NOT NULL COMMENT '来源类型 1-注册 2-签到 3-消费 4-评价 5-兑换 6-过期 7-管理员调整',
  `source_id` varchar(64) DEFAULT NULL COMMENT '来源ID(如订单号、兑换记录ID)',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `balance_after` int NOT NULL COMMENT '变动后积分余额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_source_type` (`source_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户积分记录表';
DROP TABLE IF EXISTS `user_sign_in`;
CREATE TABLE `user_sign_in` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '签到记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `sign_date` date NOT NULL COMMENT '签到日期',
  `continuous_days` int NOT NULL DEFAULT '1' COMMENT '连续签到天数',
  `points_earned` int NOT NULL DEFAULT '0' COMMENT '获得积分',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `deleted` int DEFAULT '0' COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`,`sign_date`),
  KEY `idx_sign_date` (`sign_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户签到表';
DROP TABLE IF EXISTS `vip_order`;
CREATE TABLE `vip_order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `plan_id` bigint DEFAULT NULL COMMENT 'VIP套餐ID(购买方式)',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '支付金额',
  `source` tinyint NOT NULL DEFAULT '1' COMMENT '来源 1-购买 2-订单达标自动升级',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 0-待付款 1-已付款',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `vip_start_time` datetime DEFAULT NULL COMMENT 'VIP开始时间',
  `vip_end_time` datetime DEFAULT NULL COMMENT 'VIP结束时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VIP订单表';
DROP TABLE IF EXISTS `vip_plan`;
CREATE TABLE `vip_plan` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) NOT NULL COMMENT '套餐名称',
  `price` decimal(10,2) NOT NULL COMMENT '价格',
  `duration_days` int NOT NULL COMMENT '有效天数',
  `description` varchar(500) DEFAULT NULL COMMENT '套餐描述',
  `benefits` text COMMENT '权益说明(JSON)',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态 0-禁用 1-启用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除 0-否 1-是',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='VIP套餐表';


-- ========================================
-- 基础初始化数据
-- 说明：
-- 1. 仅保留适合项目初始化与论文演示的默认数据
-- 2. 不导入测试残留商家、测试分类、测试优惠券等运行过程数据
-- 3. 默认管理员账号：admin / 123456
-- ========================================

INSERT INTO `user` (`id`, `username`, `password`, `nickname`, `user_type`, `status`)
VALUES (1, 'admin', 'd870feacbcf491a0964935c2a9cd40a3', '系统管理员', 2, 1);

INSERT INTO `category` (`id`, `parent_id`, `name`, `sort`, `status`)
VALUES
  (1, 0, '新鲜水果', 1, 1),
  (2, 0, '时令蔬菜', 2, 1),
  (3, 0, '肉禽蛋品', 3, 1),
  (4, 0, '海鲜水产', 4, 1),
  (5, 0, '粮油调味', 5, 1),
  (6, 0, '乳品烘焙', 6, 1);

INSERT INTO `member_level` (`id`, `level_name`, `level_code`, `required_points`, `discount_rate`, `icon`, `color`, `benefits`, `sort`, `status`)
VALUES
  (1, '普通会员', 'NORMAL', 0, 1.00, NULL, '#999999', '["享受平台基础服务","参与平台活动"]', 1, 1),
  (2, '银卡会员', 'SILVER', 500, 0.98, NULL, '#C0C0C0', '["98折优惠","专属客服","生日礼包","优先发货"]', 2, 1),
  (3, '金卡会员', 'GOLD', 2000, 0.95, NULL, '#FFD700', '["95折优惠","专属客服","生日礼包","优先发货","免运费券","专享活动"]', 3, 1),
  (4, '钻石会员', 'DIAMOND', 5000, 0.92, NULL, '#B9F2FF', '["92折优惠","VIP客服","生日礼包","优先发货","免运费券","专享活动","新品试用","积分翻倍"]', 4, 1);

INSERT INTO `vip_plan` (`id`, `name`, `price`, `duration_days`, `description`, `benefits`, `sort`, `status`)
VALUES
  (1, '月度会员', 12.90, 30, '适合短期体验平台会员权益的用户', '["签到双倍积分","会员专属优惠券","优先客服"]', 1, 1),
  (2, '季度会员', 32.90, 90, '适合稳定复购用户的中期会员套餐', '["签到双倍积分","会员专属优惠券","优先客服","部分券免费领取"]', 2, 1),
  (3, '年度会员', 68.00, 365, '适合作为长期会员运营的核心套餐', '["签到双倍积分","会员专属优惠券","优先客服","部分券免费领取","活动优先参与"]', 3, 1);

INSERT INTO `banner` (`id`, `title`, `image_url`, `link_url`, `link_type`, `sort`, `status`)
VALUES
  (1, '新鲜水果 每日上新', 'https://example.com/banner-fruit.jpg', '/category/1', 2, 1, 1),
  (2, '会员专享 限时领券', 'https://example.com/banner-coupon.jpg', '/coupons', 0, 2, 1),
  (3, '安心溯源 品质直达', 'https://example.com/banner-traceability.jpg', '/announcements', 0, 3, 1);

INSERT INTO `announcement` (`id`, `type`, `title`, `content`, `cover_image`, `view_count`, `sort`, `status`, `publish_time`)
VALUES
  (1, 1, '欢迎使用水果生鲜电商平台', '平台支持消费者、商家和管理员三端协同运行，可完成商品浏览、订单处理、会员运营和平台治理等核心业务。', NULL, 0, 1, 1, '2026-01-01 09:00:00'),
  (2, 2, '新用户专享活动开启', '新注册用户可领取专享优惠券，完成首单后还可获得积分奖励与会员成长值。', NULL, 0, 2, 1, '2026-01-02 09:00:00'),
  (3, 3, '如何挑选更适合的水果商品', '建议关注商品产地、规格、保质期、新鲜度说明以及溯源节点信息，从而做出更合适的购买决策。', NULL, 0, 3, 1, '2026-01-03 09:00:00');

INSERT INTO `coupon` (`id`, `title`, `coupon_type`, `discount_amount`, `discount_rate`, `minimum_amount`, `maximum_discount`, `total_quantity`, `received_quantity`, `used_quantity`, `per_user_limit`, `valid_from`, `valid_until`, `valid_days`, `applicable_categories`, `applicable_products`, `member_levels`, `points_price`, `vip_free_receive`, `description`, `status`)
VALUES
  (1, '新用户专享5元券', 3, 5.00, NULL, 0.00, NULL, 5000, 0, 0, 1, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 15, NULL, NULL, NULL, NULL, 0, '新注册用户可直接领取，无门槛使用。', 1),
  (2, '满99减10优惠券', 1, 10.00, NULL, 99.00, NULL, 5000, 0, 0, 2, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 30, NULL, NULL, NULL, NULL, 0, '适用于平台大部分商品订单，满足满减条件即可使用。', 1),
  (3, '满199减30优惠券', 1, 30.00, NULL, 199.00, NULL, 2000, 0, 0, 1, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 30, NULL, NULL, NULL, NULL, 0, '适合大额采购订单使用。', 1),
  (4, '水果专区9折券', 2, NULL, 0.90, 99.00, 25.00, 3000, 0, 0, 1, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 20, '1', NULL, NULL, NULL, 0, '适用于新鲜水果分类订单，最高优惠25元。', 1),
  (5, '50积分兑换5元券', 3, 5.00, NULL, 0.00, NULL, 1000, 0, 0, 3, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 15, NULL, NULL, NULL, 50, 0, '普通用户可使用积分兑换的无门槛优惠券。', 1),
  (6, 'VIP每周专享券', 1, 12.00, NULL, 88.00, NULL, 999999, 0, 0, 1, '2026-01-01 00:00:00', '2030-12-31 23:59:59', 7, NULL, NULL, 'VIP', NULL, 1, '供系统每周自动发放给有效VIP用户的专属优惠券。', 1);
