package com.fruit.aspect;

import com.fruit.annotation.RateLimit;
import com.fruit.common.exception.BusinessException;
import com.fruit.common.result.ResultCode;
import com.fruit.dto.LoginDTO;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        HttpServletRequest request = getRequest();
        String ip = getClientIp(request);
        String methodSignature = buildMethodSignature(joinPoint);
        String username = extractUsername(joinPoint.getArgs());
        String key = RATE_LIMIT_PREFIX + ip + ":" + username + ":" + methodSignature;

        Long currentCount = redisTemplate.opsForValue().increment(key);
        if (currentCount != null && currentCount == 1L) {
            redisTemplate.expire(key, rateLimit.time(), TimeUnit.SECONDS);
        }

        if (currentCount != null && currentCount > rateLimit.count()) {
            throw new BusinessException(ResultCode.RATE_LIMIT_EXCEEDED.getCode(), rateLimit.message());
        }

        return joinPoint.proceed();
    }

    private HttpServletRequest getRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes)) {
            throw new BusinessException(ResultCode.FAILED);
        }
        return ((ServletRequestAttributes) attributes).getRequest();
    }

    private String buildMethodSignature(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return signature.getDeclaringTypeName() + "." + signature.getMethod().getName();
    }

    private String extractUsername(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof LoginDTO) {
                return ((LoginDTO) arg).getUsername();
            }
        }
        return "anonymous";
    }

    private String getClientIp(HttpServletRequest request) {
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String headerName : headerNames) {
            String ip = request.getHeader(headerName);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }
}
