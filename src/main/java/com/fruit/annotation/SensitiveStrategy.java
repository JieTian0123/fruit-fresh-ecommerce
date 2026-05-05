package com.fruit.annotation;

import java.util.function.Function;

public enum SensitiveStrategy {
    /** 手机号脱敏: 138****1234 */
    PHONE(s -> s.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2")),
    /** 邮箱脱敏: t***@example.com */
    EMAIL(s -> s.replaceAll("(.).+(@.+)", "$1***$2")),
    /** 地址脱敏: 只保留前6个字符 + **** */
    ADDRESS(s -> s.length() > 6 ? s.substring(0, 6) + "****" : s),
    /** 姓名脱敏: 张** */
    NAME(s -> s.length() > 1 ? s.charAt(0) + "**" : s);

    private final Function<String, String> desensitizer;

    SensitiveStrategy(Function<String, String> desensitizer) {
        this.desensitizer = desensitizer;
    }

    public Function<String, String> desensitizer() {
        return desensitizer;
    }
}
