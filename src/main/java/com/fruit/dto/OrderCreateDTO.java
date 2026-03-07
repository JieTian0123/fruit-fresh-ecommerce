package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 创建订单DTO
 */
@Data
public class OrderCreateDTO {

    /**
     * 收货地址ID
     */
    @NotNull(message = "请选择收货地址")
    private Long addressId;

    /**
     * 订单备注
     */
    private String remark;

    /**
     * 商品ID（立即购买模式）
     */
    private Long productId;

    /**
     * 商品数量（立即购买模式）
     */
    private Integer quantity;

    /**
     * 优惠券ID（用户优惠券记录ID）
     */
    private Long couponId;
}
