package com.fruit.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商家端-会员客户VO
 */
@Data
public class MemberCustomerVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 会员等级名称
     */
    private String memberLevelName;

    /**
     * 在本店下单次数
     */
    private Integer orderCount;

    /**
     * 在本店消费总额
     */
    private BigDecimal totalSpend;

    /**
     * 最近下单时间
     */
    private LocalDateTime lastOrderTime;

    /**
     * 是否平台VIP (0-否 1-是)
     */
    private Integer isVip;
}
