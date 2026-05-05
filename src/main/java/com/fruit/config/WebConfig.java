package com.fruit.config;

import com.fruit.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginInterceptor loginInterceptor;

    /**
     * 跨域配置
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * 拦截器配置
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证接口
                        "/auth/**",
                        // 登录注册接口（兼容旧路径）
                        "/user/login",
                        "/user/register",
                        "/merchant/login",
                        "/merchant/register",
                        "/admin/login",
                        "/captcha",
                        // 消费者公开接口
                        "/consumer/product/**",
                        "/consumer/category/**",
                        "/consumer/shop/**",
                        "/home/**",
                        "/product/list",
                        "/product/detail/**",
                        "/category/list",
                        "/category/tree",
                        // 上传资源访问
                        "/uploads/**",
                        // Swagger
                        "/doc.html",
                        "/webjars/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        // 静态资源
                        "/favicon.ico",
                        "/error"
                );
    }
}
