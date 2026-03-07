package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 会员等级实体类
 */
@Data
@TableName("member_level")
public class MemberLevel {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 等级名称
     */
    private String levelName;

    /**
     * 等级代码
     */
    private String levelCode;

    /**
     * 所需积分
     */
    private Integer requiredPoints;

    /**
     * 折扣率 0.95表示95折
     */
    private BigDecimal discountRate;

    /**
     * 等级图标
     */
    private String icon;

    /**
     * 等级颜色
     */
    private String color;

    /**
     * 会员权益说明(JSON格式)
     */
    private String benefits;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 是否删除 0-否 1-是
     */
    @TableLogic
    private Integer deleted;
}
