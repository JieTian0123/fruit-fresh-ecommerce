-- ================================
-- 水果生鲜电商系统 - 平台公告数据库脚本
-- ================================

USE `fruit_ecommerce`;

-- ================================
-- 平台公告表
-- ================================
DROP TABLE IF EXISTS `announcement`;
CREATE TABLE `announcement` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '公告ID',
  `type` INT(1) NOT NULL COMMENT '公告类型 1-系统公告 2-活动公告 3-知识科普',
  `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
  `content` TEXT COMMENT '公告内容',
  `cover_image` VARCHAR(255) DEFAULT NULL COMMENT '封面图片',
  `view_count` INT(11) NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `sort` INT(11) DEFAULT 0 COMMENT '排序',
  `status` INT(1) NOT NULL DEFAULT 1 COMMENT '状态 0-草稿 1-发布 2-下架',
  `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` INT(1) DEFAULT 0 COMMENT '是否删除 0-否 1-是',
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_publish_time` (`publish_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台公告表';

-- 插入默认公告数据
INSERT INTO `announcement` (`type`, `title`, `content`, `status`, `publish_time`) VALUES 
(1, '欢迎来到水果生鲜电商平台', '感谢您选择我们的水果生鲜电商平台，我们致力于为您提供最新鲜、最优质的水果和生鲜产品。', 1, NOW()),
(2, '新年特惠活动开启', '全场水果8折起，购买满100元减20元，快来选购吧！', 1, NOW()),
(3, '如何挑选新鲜水果', '选购水果时，可以通过观察颜色、闻气味、按压果皮等方式来判断水果的新鲜程度...', 1, NOW());
