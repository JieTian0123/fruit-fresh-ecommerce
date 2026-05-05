package com.fruit.vo;

import com.fruit.annotation.Sensitive;
import com.fruit.annotation.SensitiveStrategy;
import lombok.Data;

/**
 * 登录VO
 */
@Data
public class LoginVO {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    @Sensitive(strategy = SensitiveStrategy.PHONE)
    private String phone;

    /**
     * 邮箱
     */
    @Sensitive(strategy = SensitiveStrategy.EMAIL)
    private String email;

    /**
     * 用户类型
     */
    private Integer userType;

    /**
     * 角色 (CONSUMER/MERCHANT/ADMIN)
     */
    private String role;

    /**
     * Token
     */
    private String token;

    /**
     * 积分
     */
    private Integer points;

    /**
     * 累计消费金额
     */
    private java.math.BigDecimal totalConsumption;
}
