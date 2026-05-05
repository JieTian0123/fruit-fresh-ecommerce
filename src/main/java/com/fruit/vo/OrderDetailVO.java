package com.fruit.vo;

import com.fruit.annotation.Sensitive;
import com.fruit.annotation.SensitiveStrategy;
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

    @Sensitive(strategy = SensitiveStrategy.NAME)
    private String receiverName;

    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String receiverPhone;

    @Sensitive(strategy = SensitiveStrategy.ADDRESS)
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
