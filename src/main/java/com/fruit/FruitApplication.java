package com.fruit;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 水果生鲜电商系统启动类
 */
@SpringBootApplication
@MapperScan("com.fruit.mapper")
@EnableScheduling
public class FruitApplication {

    public static void main(String[] args) {
        SpringApplication.run(FruitApplication.class, args);
        System.out.println("====================================");
        System.out.println("  水果生鲜电商系统启动成功!");
        System.out.println("  API文档: http://localhost:8080/doc.html");
        System.out.println("====================================");
    }
}
