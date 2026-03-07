package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 发货DTO
 */
@Data
public class DeliverDTO {

    @NotBlank(message = "订单编号不能为空")
    private String orderNo;

    @NotBlank(message = "物流公司不能为空")
    private String deliveryCompany;

    @NotBlank(message = "物流单号不能为空")
    private String deliveryNo;
}
