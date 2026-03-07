package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户积分记录实体类
 */
@Data
@TableName("user_points_log")
public class UserPointsLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 积分变动(正数为增加,负数为减少)
     */
    private Integer points;

    /**
     * 来源类型 1-注册 2-签到 3-消费 4-评价 5-兑换 6-过期 7-管理员调整
     */
    private Integer sourceType;

    /**
     * 来源ID(如订单号、兑换记录ID)
     */
    private String sourceId;

    /**
     * 描述
     */
    private String description;

    /**
     * 变动后积分余额
     */
    private Integer balanceAfter;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableLogic
    private Integer deleted;
}
