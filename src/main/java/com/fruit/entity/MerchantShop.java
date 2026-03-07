package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商家店铺实体类
 */
@Data
@TableName("merchant_shop")
public class MerchantShop {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 商家用户ID
     */
    private Long merchantId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺Logo
     */
    private String logo;

    /**
     * 店铺描述
     */
    private String description;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 营业执照
     */
    private String businessLicense;

    /**
     * 状态 0-禁用 1-启用 2-待审核
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
