-- ================================
-- 测试数据 - 商家和商品
-- ================================

USE `fruit_ecommerce`;

-- 插入测试商家用户 (用户名: merchant1, 密码: 123456)
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `user_type`, `status`) VALUES 
('merchant1', 'e10adc3949ba59abbe56e057f20f883e', '丰收果园', '13800138001', 1, 1);

-- 获取商家ID (假设为3)
SET @merchant_id = LAST_INSERT_ID();

-- 插入商家店铺
INSERT INTO `merchant_shop` (`merchant_id`, `shop_name`, `logo`, `description`, `province`, `city`, `district`, `address`, `contact_phone`, `status`) VALUES 
(@merchant_id, '丰收果园旗舰店', NULL, '专业水果生鲜供应商，产地直发，新鲜到家', '广东省', '深圳市', '南山区', '科技园路88号', '13800138001', 1);

SET @shop_id = LAST_INSERT_ID();

-- 插入测试商品 - 新鲜水果分类 (category_id = 1)
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`) VALUES 
(@merchant_id, @shop_id, 1, '红富士苹果', '山东烟台红富士 脆甜多汁 5斤装', 'https://img.alicdn.com/imgextra/i4/2215256808481/O1CN01MhGLTD1sUzF4u4jgO_!!2215256808481.jpg', 29.90, 39.90, 100, 256, '斤', 1),
(@merchant_id, @shop_id, 1, '进口车厘子', '智利进口JJ级 2斤礼盒装', 'https://img.alicdn.com/imgextra/i3/2215256808481/O1CN01TgOLMR1sUzF1VIRxZ_!!2215256808481.jpg', 89.90, 129.90, 50, 128, '盒', 1),
(@merchant_id, @shop_id, 1, '新疆阿克苏苹果', '冰糖心苹果 10斤装', 'https://img.alicdn.com/imgextra/i4/2215256808481/O1CN01ZKFpJQ1sUzF5LkjnL_!!2215256808481.jpg', 49.90, 69.90, 200, 512, '箱', 1),
(@merchant_id, @shop_id, 1, '海南金煌芒果', '当季新鲜 5斤装', 'https://img.alicdn.com/imgextra/i1/2215256808481/O1CN01qjKoVe1sUzF2XQJZQ_!!2215256808481.jpg', 35.90, 45.90, 80, 89, '斤', 1),
(@merchant_id, @shop_id, 1, '云南蓝莓', '有机蓝莓 4盒装 125g/盒', 'https://img.alicdn.com/imgextra/i2/2215256808481/O1CN01Qp2nKu1sUzF3VQJTU_!!2215256808481.jpg', 59.90, 79.90, 60, 156, '盒', 1),
(@merchant_id, @shop_id, 1, '泰国山竹', '进口5A级 3斤装', 'https://img.alicdn.com/imgextra/i3/2215256808481/O1CN01YdRPKS1sUzF4u4k2C_!!2215256808481.jpg', 45.90, 55.90, 40, 78, '斤', 1);

-- 插入测试商品 - 时令蔬菜分类 (category_id = 2)
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`) VALUES 
(@merchant_id, @shop_id, 2, '有机西兰花', '云南高山有机蔬菜 500g', 'https://img.alicdn.com/imgextra/i4/2215256808481/O1CN01xKl2rD1sUzF6LkjnM_!!2215256808481.jpg', 12.90, 15.90, 150, 89, '份', 1),
(@merchant_id, @shop_id, 2, '新鲜番茄', '草莓番茄 沙瓤多汁 2斤装', 'https://img.alicdn.com/imgextra/i1/2215256808481/O1CN01aBcDEF1sUzF7VQJTU_!!2215256808481.jpg', 15.90, 19.90, 120, 156, '斤', 1),
(@merchant_id, @shop_id, 2, '水果黄瓜', '山东小黄瓜 清脆爽口 3斤装', 'https://img.alicdn.com/imgextra/i2/2215256808481/O1CN01bCdEFG1sUzF8VQJUV_!!2215256808481.jpg', 18.90, 23.90, 100, 67, '斤', 1);

-- 插入测试商品 - 肉禽蛋品分类 (category_id = 3)  
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`) VALUES 
(@merchant_id, @shop_id, 3, '土鸡蛋', '农家散养土鸡蛋 30枚装', 'https://img.alicdn.com/imgextra/i3/2215256808481/O1CN01cDeEFH1sUzF9VQJVW_!!2215256808481.jpg', 35.90, 42.90, 80, 234, '盒', 1),
(@merchant_id, @shop_id, 3, '黑猪五花肉', '黑毛猪肉 500g', 'https://img.alicdn.com/imgextra/i4/2215256808481/O1CN01dEfFGI1sUzFAVQJWX_!!2215256808481.jpg', 45.90, 55.90, 50, 45, '份', 1);

-- 插入测试商品 - 海鲜水产分类 (category_id = 4)
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`) VALUES 
(@merchant_id, @shop_id, 4, '厄瓜多尔白虾', '进口大白虾 30-40头/斤 2斤装', 'https://img.alicdn.com/imgextra/i1/2215256808481/O1CN01eFgGHJ1sUzFBVQJXY_!!2215256808481.jpg', 89.90, 109.90, 30, 67, '盒', 1),
(@merchant_id, @shop_id, 4, '挪威三文鱼', '进口冰鲜三文鱼 刺身级 400g', 'https://img.alicdn.com/imgextra/i2/2215256808481/O1CN01fGhHIK1sUzFCVQJYZ_!!2215256808481.jpg', 98.90, 128.90, 25, 34, '份', 1);

-- 更新用户 aaaa 的昵称
UPDATE `user` SET `nickname` = '测试用户' WHERE `username` = 'aaaa';

SELECT '测试数据插入完成!' AS message;
