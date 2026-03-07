package com.fruit.vo;

import lombok.Data;

import java.util.List;

/**
 * 分类树VO
 */
@Data
public class CategoryTreeVO {

    private Long id;

    private Long parentId;

    private String name;

    private String icon;

    private Integer sort;

    private List<CategoryTreeVO> children;
}
