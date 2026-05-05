package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fruit.annotation.Sensitive;
import com.fruit.annotation.SensitiveStrategy;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 */
@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 订单编号
     */
    private String orderNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 商家ID
     */
    private Long merchantId;

    /**
     * 收货人姓名
     */
    @Sensitive(strategy = SensitiveStrategy.NAME)
    private String receiverName;

    /**
     * 收货人电话
     */
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String receiverPhone;

    /**
     * 收货地址
     */
    @Sensitive(strategy = SensitiveStrategy.ADDRESS)
    private String receiverAddress;

    /**
     * 商品总金额
     */
    private BigDecimal totalAmount;

    /**
     * 运费
     */
    private BigDecimal freightAmount;

    /**
     * 优惠金额
     */
    private BigDecimal discountAmount;

    /**
     * 实付金额
     */
    private BigDecimal payAmount;

    /**
     * 用户优惠券ID
     */
    private Long couponId;

    /**
     * 优惠券抵扣金额
     */
    private BigDecimal couponAmount;

    /**
     * 支付方式 1-微信 2-支付宝
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单状态 0-待付款 1-待发货 2-待收货 3-已完成 4-已取消
     */
    private Integer status;

    /**
     * 物流公司
     */
    private String deliveryCompany;

    /**
     * 物流单号
     */
    private String deliveryNo;

    /**
     * 发货时间
     */
    private LocalDateTime deliveryTime;

    /**
     * 收货时间
     */
    private LocalDateTime receiveTime;

    /**
     * 订单备注
     */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;

    @TableField(exist = false)
    private java.util.List<OrderItem> orderItems;

    /**
     * 商家名称（非数据库字段，用于展示）
     */
    @TableField(exist = false)
    private String merchantName;
}
