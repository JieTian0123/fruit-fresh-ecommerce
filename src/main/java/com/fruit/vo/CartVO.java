package com.fruit.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 购物车VO
 */
@Data
public class CartVO {

    private Long id;

    private Long userId;

    private Long productId;

    private Long merchantId;

    private Integer quantity;

    private Integer selected;

    /**
     * 商品名称
     */
    private String productName;

    /**
     * 商品图片
     */
    private String productImage;

    /**
     * 商品单价
     */
    private BigDecimal price;

    /**
     * 库存
     */
    private Integer stock;

    private LocalDateTime createTime;
}
