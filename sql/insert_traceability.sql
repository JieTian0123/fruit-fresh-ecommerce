-- 商品1: 红富士苹果 溯源数据
INSERT INTO product_traceability (product_id, node_type, node_name, description, location, operator, temperature, humidity, occurred_time) VALUES
(1, 1, '果园采摘', '精选红富士苹果，人工采摘确保果实完整', '山东烟台栖霞市果园', '张师傅', 18.0, 65.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 2, '产地质检', '外观检测合格，糖度检测达标(14.5°Brix)，无农残', '烟台栖霞质检中心', '质检员李明', 20.0, 55.0, DATE_SUB(NOW(), INTERVAL 3 DAY) + INTERVAL 4 HOUR),
(1, 3, '冷链入库', '分拣包装完成，入冷库保鲜存储', '烟台冷链物流中心', '仓管王强', 2.0, 90.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(1, 4, '冷链出库', '订单配货完成，冷链车装车发运', '烟台冷链物流中心', '调度刘洋', 3.0, 88.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(1, 5, '末端配送', '全程冷链配送，送达客户手中', '本地配送站', '配送员赵刚', 5.0, 80.0, DATE_SUB(NOW(), INTERVAL 6 HOUR));

-- 商品2: 进口车厘子 溯源数据
INSERT INTO product_traceability (product_id, node_type, node_name, description, location, operator, temperature, humidity, occurred_time) VALUES
(2, 1, '果园采摘', '智利优质车厘子，机械辅助采摘', '智利圣地亚哥果园', 'Carlos Martinez', 15.0, 60.0, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 2, '产地质检', '出口级品质检测，农残检测合格，果径28mm+', '智利出口质检站', '质检员Ana', 18.0, 50.0, DATE_SUB(NOW(), INTERVAL 5 DAY) + INTERVAL 6 HOUR),
(2, 3, '冷链入库', '空运抵达国内，海关检疫通过，入保税冷库', '上海浦东保税冷库', '仓管周明', 0.5, 92.0, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(2, 4, '冷链出库', '分拣配货完成，冷链专车转运', '上海浦东保税冷库', '调度陈伟', 1.0, 90.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(2, 5, '末端配送', '冷链到家，全程温控记录可查', '本地配送站', '配送员孙磊', 4.0, 82.0, DATE_SUB(NOW(), INTERVAL 4 HOUR));

-- 商品3: 新疆阿克苏苹果 溯源数据
INSERT INTO product_traceability (product_id, node_type, node_name, description, location, operator, temperature, humidity, occurred_time) VALUES
(3, 1, '果园采摘', '冰糖心苹果，手工采摘保证品质', '新疆阿克苏温宿县果园', '艾买提师傅', 16.0, 45.0, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 2, '产地质检', '糖心率检测95%以上，甜度达标', '阿克苏农产品质检站', '质检员王芳', 19.0, 50.0, DATE_SUB(NOW(), INTERVAL 4 DAY) + INTERVAL 5 HOUR),
(3, 3, '冷链入库', '预冷处理后入冷库，保持冰糖心口感', '阿克苏冷链中心', '仓管阿里木', 1.0, 92.0, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(3, 4, '冷链出库', '航空冷链发运，全程温控', '阿克苏冷链中心', '调度张磊', 2.0, 90.0, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(3, 5, '末端配送', '冷链到家配送', '本地配送站', '配送员李伟', 5.0, 78.0, DATE_SUB(NOW(), INTERVAL 3 HOUR));

SELECT product_id, COUNT(*) as nodes FROM product_traceability GROUP BY product_id;
