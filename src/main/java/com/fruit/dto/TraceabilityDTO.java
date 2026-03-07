package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 溯源DTO
 */
@Data
public class TraceabilityDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "节点类型不能为空")
    private Integer nodeType;

    @NotBlank(message = "节点名称不能为空")
    private String nodeName;

    private String description;
    private String location;
    private String operator;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private String imageUrl;

    @NotNull(message = "发生时间不能为空")
    private LocalDateTime occurredTime;
}
