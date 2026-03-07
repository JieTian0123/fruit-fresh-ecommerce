package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 店铺DTO
 */
@Data
public class ShopDTO {

    @NotBlank(message = "店铺名称不能为空")
    private String shopName;

    private String logo;

    private String description;

    private String province;

    private String city;

    private String district;

    private String address;

    @NotBlank(message = "联系电话不能为空")
    private String contactPhone;

    private String businessLicense;
}
