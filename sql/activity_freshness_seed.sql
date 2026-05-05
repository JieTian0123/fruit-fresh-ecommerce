-- 首页活动演示数据：保质期生鲜、临期特惠、新鲜到店、热销榜单
-- 可重复执行：同名商品存在时不会重复插入。

INSERT INTO category (parent_id, name, icon, sort, status, create_time, update_time, deleted)
SELECT 0, '新鲜水果', NULL, 1, 1, NOW(), NOW(), 0
WHERE NOT EXISTS (
  SELECT 1 FROM category WHERE name = '新鲜水果' AND deleted = 0
);

SET @fruit_cat := (SELECT id FROM category WHERE name = '新鲜水果' AND deleted = 0 ORDER BY id DESC LIMIT 1);
SET @veg_cat := COALESCE((SELECT id FROM category WHERE name = '时令蔬菜' AND deleted = 0 ORDER BY id LIMIT 1), @fruit_cat);
SET @sea_cat := COALESCE((SELECT id FROM category WHERE name = '海鲜水产' AND deleted = 0 ORDER BY id LIMIT 1), @fruit_cat);
SET @dairy_cat := COALESCE((SELECT id FROM category WHERE name = '乳品烘焙' AND deleted = 0 ORDER BY id LIMIT 1), @fruit_cat);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 3, 1, @fruit_cat, '高山蓝莓鲜盒', '今日到仓，果粉完整，冷链保鲜', 'https://images.unsplash.com/photo-1498557850523-fd3d118b962e?w=800', '高山蓝莓鲜盒，适合早餐、烘焙和鲜食，0-4℃冷藏保存。', 29.90, 36.90, 120, 86, '盒', 7, CURDATE(), '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '高山蓝莓鲜盒' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 8, 2, @fruit_cat, '现摘阳光玫瑰葡萄', '脆甜多汁，产地直发', 'https://images.unsplash.com/photo-1537640538966-79f369143f8f?w=800', '阳光玫瑰葡萄，果粒饱满，适合家庭鲜食。', 39.90, 49.90, 96, 128, '盒', 5, CURDATE() - INTERVAL 1 DAY, '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '现摘阳光玫瑰葡萄' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 10, 4, @veg_cat, '清晨有机生菜', '叶片鲜嫩，适合沙拉和轻食', 'https://images.unsplash.com/photo-1622206151226-18ca2c9ab4a1?w=800', '清晨采收有机生菜，建议冷藏保存并尽快食用。', 8.90, 12.90, 150, 76, '颗', 4, CURDATE(), '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '清晨有机生菜' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 9, 3, @sea_cat, '冰鲜基围虾', '冰鲜到店，肉质紧实', 'https://images.unsplash.com/photo-1565680018434-b513d5e5fd47?w=800', '冰鲜基围虾，适合白灼、清炒，建议低温保存。', 45.90, 55.90, 68, 95, '斤', 3, CURDATE(), '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '冰鲜基围虾' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 3, 1, @fruit_cat, '草莓家庭装临期优选', '保质期过半，适合当天鲜食', 'https://images.unsplash.com/photo-1464965911861-746a04b4bca6?w=800', '草莓家庭装，系统按保质期自动计算当前优惠价。', 24.90, 35.90, 58, 188, '盒', 5, CURDATE() - INTERVAL 3 DAY, '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '草莓家庭装临期优选' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 10, 4, @veg_cat, '娃娃菜临期特惠', '清甜脆嫩，煮汤火锅都适合', 'https://images.unsplash.com/photo-1594282486552-05b4d80fbb9f?w=800', '娃娃菜临期特惠，建议冷藏保存并尽快食用。', 5.90, 8.90, 90, 160, '包', 6, CURDATE() - INTERVAL 4 DAY, '0-4℃冷藏', 'B', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '娃娃菜临期特惠' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 8, 2, @dairy_cat, '鲜牛奶临期特惠', '低温鲜奶，保质期过半', 'https://images.unsplash.com/photo-1550583724-b2692b85b150?w=800', '低温鲜牛奶，适合早餐搭配，系统自动给出临期优惠。', 12.90, 18.90, 72, 142, '瓶', 7, CURDATE() - INTERVAL 5 DAY, '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '鲜牛奶临期特惠' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 9, 3, @sea_cat, '三文鱼切片临期优选', '冰鲜切片，低温锁鲜', 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?w=800', '三文鱼切片临期优选，适合煎烤，建议尽快食用。', 59.90, 79.90, 36, 126, '盒', 4, CURDATE() - INTERVAL 3 DAY, '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '三文鱼切片临期优选' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 9, 3, @fruit_cat, '海南贵妃芒热销装', '香甜浓郁，平台热销', 'https://images.unsplash.com/photo-1553279768-865429fa0078?w=800', '海南贵妃芒，甜度高，适合家庭分享。', 32.90, 39.90, 140, 620, '箱', 7, CURDATE() - INTERVAL 1 DAY, '阴凉通风', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '海南贵妃芒热销装' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 10, 4, @veg_cat, '云南甜玉米热销装', '鲜甜多汁，蒸煮皆宜', 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?w=800', '云南甜玉米，颗粒饱满，平台高复购商品。', 16.90, 22.90, 160, 540, '袋', 6, CURDATE() - INTERVAL 1 DAY, '冷藏保存', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '云南甜玉米热销装' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 9, 3, @sea_cat, '挪威鳕鱼排热销装', '肉质细嫩，低脂高蛋白', 'https://images.unsplash.com/photo-1601314212732-a26cd33050fd?w=800', '挪威鳕鱼排，适合煎烤和宝宝辅食。', 49.90, 65.90, 82, 488, '袋', 5, CURDATE() - INTERVAL 1 DAY, '-18℃冷冻', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '挪威鳕鱼排热销装' AND deleted = 0);

INSERT INTO product (merchant_id, shop_id, category_id, name, subtitle, main_image, detail, price, original_price, stock, sales, unit, shelf_life_days, production_date, storage_condition, quality_grade, status, create_time, update_time, deleted)
SELECT 8, 2, @dairy_cat, '低温酸奶组合', '酸甜顺滑，早餐搭配', 'https://images.unsplash.com/photo-1571212515416-fef01fc43637?w=800', '低温酸奶组合，冷链配送，适合家庭早餐。', 19.90, 26.90, 118, 430, '组', 10, CURDATE() - INTERVAL 2 DAY, '0-4℃冷藏', 'A', 1, NOW(), NOW(), 0
WHERE NOT EXISTS (SELECT 1 FROM product WHERE name = '低温酸奶组合' AND deleted = 0);
