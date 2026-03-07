package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品溯源实体类
 */
@Data
@TableName("product_traceability")
public class ProductTraceability {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 节点类型 1-采摘 2-质检 3-入库 4-出库 5-配送 */
    private Integer nodeType;

    /** 节点名称 */
    private String nodeName;

    /** 描述 */
    private String description;

    /** 地点 */
    private String location;

    /** 操作人 */
    private String operator;

    /** 温度(℃) */
    private BigDecimal temperature;

    /** 湿度(%) */
    private BigDecimal humidity;

    /** 图片 */
    private String imageUrl;

    /** 发生时间 */
    private LocalDateTime occurredTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
