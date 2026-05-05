package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券实体类
 */
@Data
@TableName("coupon")
public class Coupon {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 优惠券标题
     */
    private String title;

    /**
     * 优惠券类型 1-满减券 2-折扣券 3-无门槛券
     */
    private Integer couponType;

    /**
     * 优惠金额(满减券、无门槛券)
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率(折扣券) 0.9表示9折
     */
    private BigDecimal discountRate;

    /**
     * 最低消费金额
     */
    private BigDecimal minimumAmount;

    /**
     * 最高优惠金额(折扣券)
     */
    private BigDecimal maximumDiscount;

    /**
     * 发行总量
     */
    private Integer totalQuantity;

    /**
     * 已领取数量
     */
    private Integer receivedQuantity;

    /**
     * 已使用数量
     */
    private Integer usedQuantity;

    /**
     * 每人限领数量
     */
    private Integer perUserLimit;

    /**
     * 有效期开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validFrom;

    /**
     * 有效期结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime validUntil;

    /**
     * 领取后有效天数(NULL表示使用固定时间)
     */
    private Integer validDays;

    /**
     * 适用分类ID(逗号分隔,NULL表示全场)
     */
    private String applicableCategories;

    /**
     * 适用商品ID(逗号分隔,NULL表示全场)
     */
    private String applicableProducts;

    /**
     * 适用会员等级(逗号分隔,NULL表示全部)
     */
    private String memberLevels;

    /**
     * 积分兑换价格(NULL表示不可兑换)
     */
    private Integer pointsPrice;

    /**
     * VIP会员是否可免费领取 0-否 1-是
     */
    private Integer vipFreeReceive;

    /**
     * 使用说明
     */
    private String description;

    /**
     * 状态 0-禁用 1-启用 2-已结束
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-否 1-是
     */
    @TableLogic
    private Integer deleted;
}
