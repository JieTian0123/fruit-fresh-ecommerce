package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 商品名称
     */
    private String name;

    /**
     * 副标题
     */
    private String subtitle;

    /**
     * 主图
     */
    private String mainImage;

    /**
     * 子图（JSON数组）
     */
    private String subImages;

    /**
     * 商品详情
     */
    private String detail;

    /**
     * 价格
     */
    private BigDecimal price;

    /**
     * 原价
     */
    private BigDecimal originalPrice;

    /**
     * 库存
     */
    private Integer stock;

    /**
     * 销量
     */
    private Integer sales;

    /**
     * 单位
     */
    private String unit;

    /**
     * 重量(kg)
     */
    private BigDecimal weight;

    /**
     * 保质期天数
     */
    private Integer shelfLifeDays;

    /**
     * 生产/采摘日期
     */
    private LocalDate productionDate;

    /**
     * 存储条件（如：0-4℃冷藏）
     */
    private String storageCondition;

    /**
     * 品质等级 A/B/C
     */
    private String qualityGrade;

    /**
     * 当前价格（基于新鲜度折扣，不存数据库）
     */
    @TableField(exist = false)
    private BigDecimal currentPrice;

    /**
     * 折扣标签（不存数据库）
     */
    @TableField(exist = false)
    private String discountLabel;

    /**
     * 店铺名称（不存数据库，关联查询）
     */
    @TableField(exist = false)
    private String merchantName;

    /**
     * 分类名称（不存数据库，关联查询）
     */
    @TableField(exist = false)
    private String categoryName;

    /**
     * 店铺Logo（不存数据库，关联查询）
     */
    @TableField(exist = false)
    private String shopLogo;

    /**
     * 状态 0-下架 1-上架 2-待审核
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
