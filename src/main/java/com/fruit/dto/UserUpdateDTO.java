package com.fruit.dto;

import lombok.Data;

/**
 * 更新用户信息DTO
 */
@Data
public class UserUpdateDTO {

    private String nickname;

    private String phone;

    private String email;

    private String avatar;
}
