package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 收货地址DTO
 */
@Data
public class AddressDTO {

    private Long id;

    @NotBlank(message = "收货人姓名不能为空")
    private String receiverName;

    @NotBlank(message = "收货人电话不能为空")
    private String receiverPhone;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String detailAddress;

    /**
     * 是否默认
     */
    private Integer isDefault;
}
