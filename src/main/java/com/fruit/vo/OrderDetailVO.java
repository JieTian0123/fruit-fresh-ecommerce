package com.fruit.vo;

import com.fruit.entity.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单详情VO
 */
@Data
public class OrderDetailVO {

    private Long id;

    private String orderNo;

    private Long userId;

    private Long merchantId;

    private String receiverName;

    private String receiverPhone;

    private String receiverAddress;

    private BigDecimal totalAmount;

    private BigDecimal freightAmount;

    private BigDecimal discountAmount;

    private BigDecimal payAmount;

    private Integer payType;

    private LocalDateTime payTime;

    private Integer status;

    private String statusName;

    private String deliveryCompany;

    private String deliveryNo;

    private LocalDateTime deliveryTime;

    private LocalDateTime receiveTime;

    private String remark;

    private LocalDateTime createTime;

    /**
     * 订单明细
     */
    private List<OrderItem> orderItems;
}
