USE `fruit_ecommerce`;

-- 额外商家账号
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `user_type`, `status`)
SELECT 'fruit_merchant_a', 'e10adc3949ba59abbe56e057f20f883e', '果园直发', '13800000031', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'fruit_merchant_a');

INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `user_type`, `status`)
SELECT 'fruit_merchant_b', 'e10adc3949ba59abbe56e057f20f883e', '热带鲜选', '13800000032', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'fruit_merchant_b');

INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `user_type`, `status`)
SELECT 'fruit_merchant_c', 'e10adc3949ba59abbe56e057f20f883e', '清晨菜篮', '13800000033', 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `user` WHERE `username` = 'fruit_merchant_c');

-- 额外店铺
INSERT INTO `merchant_shop` (`merchant_id`, `shop_name`, `description`, `province`, `city`, `district`, `address`, `contact_phone`, `status`)
SELECT u.`id`, '果园直发旗舰店', '主打当季苹果、梨和葡萄，支持当日分拣发货', '山东省', '烟台市', '福山区', '福海路88号', '13800000031', 1
FROM `user` u
WHERE u.`username` = 'fruit_merchant_a'
  AND NOT EXISTS (SELECT 1 FROM `merchant_shop` WHERE `shop_name` = '果园直发旗舰店');

INSERT INTO `merchant_shop` (`merchant_id`, `shop_name`, `description`, `province`, `city`, `district`, `address`, `contact_phone`, `status`)
SELECT u.`id`, '热带鲜选专营店', '芒果、榴莲、椰青等热带水果现摘现发', '海南省', '三亚市', '吉阳区', '迎宾路66号', '13800000032', 1
FROM `user` u
WHERE u.`username` = 'fruit_merchant_b'
  AND NOT EXISTS (SELECT 1 FROM `merchant_shop` WHERE `shop_name` = '热带鲜选专营店');

INSERT INTO `merchant_shop` (`merchant_id`, `shop_name`, `description`, `province`, `city`, `district`, `address`, `contact_phone`, `status`)
SELECT u.`id`, '清晨菜篮生鲜店', '每日补货的蔬菜与轻食搭配专区', '浙江省', '杭州市', '余杭区', '文一西路18号', '13800000033', 1
FROM `user` u
WHERE u.`username` = 'fruit_merchant_c'
  AND NOT EXISTS (SELECT 1 FROM `merchant_shop` WHERE `shop_name` = '清晨菜篮生鲜店');

-- 额外商品：果园直发旗舰店
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 1, '烟台红富士苹果', '脆甜多汁，整箱直发', 'https://images.unsplash.com/photo-1567306226416-28f0efdc88ce?auto=format&fit=crop&w=800&q=80', '精选烟台红富士，口感清甜，适合家庭囤货。', 29.90, 36.90, 120, 38, '5斤/箱', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '果园直发旗舰店'
WHERE u.`username` = 'fruit_merchant_a'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '烟台红富士苹果');

INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 1, '皇冠雪梨', '清润爽口，适合鲜食', 'https://images.unsplash.com/photo-1514756331096-242fdeb70d4a?auto=format&fit=crop&w=800&q=80', '当季雪梨，果肉细腻，适合日常补水。', 18.80, 24.80, 88, 21, '4斤/箱', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '果园直发旗舰店'
WHERE u.`username` = 'fruit_merchant_a'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '皇冠雪梨');

INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 1, '阳光玫瑰葡萄', '颗粒饱满，清香甘甜', 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?auto=format&fit=crop&w=800&q=80', '冷链配送，果粉完整，甜度稳定。', 39.90, 49.90, 66, 17, '2斤/箱', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '果园直发旗舰店'
WHERE u.`username` = 'fruit_merchant_a'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '阳光玫瑰葡萄');

-- 额外商品：热带鲜选专营店
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 1, '海南贵妃芒', '果香浓郁，甜而不腻', 'https://images.unsplash.com/photo-1553279768-865429fa0078?auto=format&fit=crop&w=800&q=80', '海南产地直发，成熟度高，适合鲜切和直接食用。', 26.80, 32.80, 96, 45, '3斤/箱', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '热带鲜选专营店'
WHERE u.`username` = 'fruit_merchant_b'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '海南贵妃芒');

INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 1, '泰国金枕榴莲', '果肉软糯，适合即食', 'https://images.unsplash.com/photo-1615485290382-441e4d049cb5?auto=format&fit=crop&w=800&q=80', '精选金枕榴莲，冷链运输，到货即可开吃。', 89.00, 99.00, 40, 13, '1个', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '热带鲜选专营店'
WHERE u.`username` = 'fruit_merchant_b'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '泰国金枕榴莲');

INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 1, '鲜开椰青', '清甜椰汁，适合冰镇', 'https://images.unsplash.com/photo-1581375321224-79da6fd32f6c?auto=format&fit=crop&w=800&q=80', '现采椰青，椰汁清爽，夏季热销。', 22.90, 28.90, 150, 52, '4个装', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '热带鲜选专营店'
WHERE u.`username` = 'fruit_merchant_b'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '鲜开椰青');

-- 额外商品：清晨菜篮生鲜店
INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 2, '有机生菜', '新鲜脆嫩，适合沙拉', 'https://images.unsplash.com/photo-1622205313162-be1d5712a43d?auto=format&fit=crop&w=800&q=80', '每日清晨采摘，适合轻食和火锅搭配。', 9.90, 12.90, 180, 29, '300g/份', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '清晨菜篮生鲜店'
WHERE u.`username` = 'fruit_merchant_c'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '有机生菜');

INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 2, '贝贝南瓜', '粉糯香甜，蒸煮都适合', 'https://images.unsplash.com/photo-1502741338009-cac2772e18bc?auto=format&fit=crop&w=800&q=80', '小果型南瓜，口感绵密，适合宝宝辅食。', 15.80, 19.80, 72, 18, '2个装', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '清晨菜篮生鲜店'
WHERE u.`username` = 'fruit_merchant_c'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '贝贝南瓜');

INSERT INTO `product` (`merchant_id`, `shop_id`, `category_id`, `name`, `subtitle`, `main_image`, `detail`, `price`, `original_price`, `stock`, `sales`, `unit`, `status`)
SELECT u.`id`, s.`id`, 6, '即食牛油果酸奶杯', '轻食早餐搭配', 'https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=800&q=80', '轻食组合款，适合作为早餐或下午茶。', 16.90, 21.90, 55, 11, '1杯', 1
FROM `user` u
JOIN `merchant_shop` s ON s.`merchant_id` = u.`id` AND s.`shop_name` = '清晨菜篮生鲜店'
WHERE u.`username` = 'fruit_merchant_c'
  AND NOT EXISTS (SELECT 1 FROM `product` WHERE `name` = '即食牛油果酸奶杯');
