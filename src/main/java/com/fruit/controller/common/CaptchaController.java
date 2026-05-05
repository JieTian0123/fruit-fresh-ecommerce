package com.fruit.controller.common;

import cn.hutool.core.lang.UUID;
import com.fruit.common.result.Result;
import com.wf.captcha.SpecCaptcha;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class CaptchaController {

    private static final String CAPTCHA_PREFIX = "captcha:";

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        SpecCaptcha captcha = new SpecCaptcha(130, 48);
        String uuid = UUID.randomUUID().toString(true);
        redisTemplate.opsForValue().set(CAPTCHA_PREFIX + uuid, captcha.text(), 2, TimeUnit.MINUTES);

        Map<String, String> data = new HashMap<>(2);
        data.put("uuid", uuid);
        data.put("image", captcha.toBase64());
        return Result.success(data);
    }
}
