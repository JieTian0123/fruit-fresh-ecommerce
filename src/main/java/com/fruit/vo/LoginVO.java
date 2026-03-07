package com.fruit.vo;

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
    private String phone;

    /**
     * 邮箱
     */
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
}
