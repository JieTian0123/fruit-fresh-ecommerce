package com.fruit.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户优惠券VO（包含优惠券详细信息）
 */
@Data
public class UserCouponVO {

    /**
     * 用户优惠券ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 优惠券ID
     */
    private Long couponId;

    /**
     * 状态 0-未使用 1-已使用 2-已过期
     */
    private Integer status;

    /**
     * 领取时间
     */
    private LocalDateTime receiveTime;

    /**
     * 使用时间
     */
    private LocalDateTime useTime;

    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 用户优惠券有效期开始时间
     */
    private LocalDateTime validFrom;

    /**
     * 用户优惠券有效期结束时间
     */
    private LocalDateTime validUntil;

    // ========== 以下为关联的优惠券信息 ==========

    /**
     * 优惠券标题
     */
    private String title;

    /**
     * 优惠券描述
     */
    private String description;

    /**
     * 优惠券类型 1-满减券 2-折扣券
     */
    private Integer couponType;

    /**
     * 满减金额（满减券）
     */
    private BigDecimal discountAmount;

    /**
     * 折扣率（折扣券，如0.8表示8折）
     */
    private BigDecimal discountRate;

    /**
     * 最低消费金额（满多少可用）
     */
    private BigDecimal minimumAmount;

    /**
     * 最大优惠金额（折扣券封顶）
     */
    private BigDecimal maximumDiscount;
}
