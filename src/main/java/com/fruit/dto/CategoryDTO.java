package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 分类DTO
 */
@Data
public class CategoryDTO {

    private Long id;

    private Long parentId;

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String icon;

    private Integer sort;

    private Integer status;
}
