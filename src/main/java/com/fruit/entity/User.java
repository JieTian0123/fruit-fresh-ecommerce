package com.fruit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 会员等级ID
     */
    private Long memberLevelId;

    /**
     * 用户积分
     */
    private Integer points;

    /**
     * 累计消费金额
     */
    private java.math.BigDecimal totalConsumption;

    /**
     * VIP状态 0-非VIP 1-VIP
     */
    private Integer isVip;

    /**
     * VIP到期时间
     */
    private LocalDateTime vipExpireTime;

    /**
     * 已完成订单数(缓存)
     */
    private Integer vipOrderCount;

    /**
     * 性别 0-未知 1-男 2-女
     */
    private Integer gender;

    /**
     * 用户类型 0-消费者 1-商家 2-管理员
     */
    private Integer userType;

    /**
     * 状态 0-禁用 1-启用 2-待审核
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
     * 逻辑删除
     */
    @TableLogic
    private Integer deleted;
}
