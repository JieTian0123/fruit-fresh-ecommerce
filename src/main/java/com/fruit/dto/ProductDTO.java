package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 商品DTO
 */
@Data
public class ProductDTO {

    private Long id;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String subtitle;

    private String mainImage;

    private String subImages;

    private String detail;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private String unit;

    private BigDecimal weight;

    private Integer shelfLifeDays;
    private LocalDate productionDate;
    private String storageCondition;
    private String qualityGrade;
}
