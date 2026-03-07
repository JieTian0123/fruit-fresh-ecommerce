package com.fruit.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 订单状态枚举
 */
@Getter
@AllArgsConstructor
public enum OrderStatusEnum {

    PENDING_PAYMENT(0, "待付款"),
    PENDING_DELIVERY(1, "待发货"),
    PENDING_RECEIVE(2, "待收货"), // 发货后即为此状态
    COMPLETED(3, "已完成"),
    CANCELLED(4, "已取消"),
    REFUNDING(5, "退款中"), // 数据库可能未定义，追加在后
    REFUNDED(6, "已退款"); // 数据库可能未定义，追加在后

    private final Integer code;
    private final String desc;

    public static OrderStatusEnum getByCode(Integer code) {
        for (OrderStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
