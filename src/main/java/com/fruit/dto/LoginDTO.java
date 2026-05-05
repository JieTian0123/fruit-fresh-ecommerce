package com.fruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 登录DTO
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    /**
     * 验证码UUID
     */
    @NotBlank(message = "验证码UUID不能为空")
    private String captchaUuid;
}
