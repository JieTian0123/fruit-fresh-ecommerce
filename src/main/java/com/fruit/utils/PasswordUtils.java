package com.fruit.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 使用 BCrypt 加密算法，比 MD5 更安全
 */
public class PasswordUtils {

    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    /**
     * 加密密码
     * BCrypt 会自动生成随机盐值，每次加密结果不同
     */
    public static String encrypt(String password) {
        return ENCODER.encode(password);
    }

    /**
     * 验证密码
     * BCrypt 会从加密后的密码中提取盐值进行验证
     */
    public static boolean verify(String password, String encryptedPassword) {
        // 兼容旧的 MD5 密码格式（32位十六进制）
        if (encryptedPassword != null && encryptedPassword.length() == 32 
                && encryptedPassword.matches("^[a-f0-9]+$")) {
            // 旧密码使用 MD5 + 盐值验证
            String oldSalt = "FruitEcommerce2024";
            String md5Hash = cn.hutool.crypto.digest.DigestUtil.md5Hex(password + oldSalt);
            return md5Hash.equals(encryptedPassword);
        }
        // 新密码使用 BCrypt 验证
        return ENCODER.matches(password, encryptedPassword);
    }
}
