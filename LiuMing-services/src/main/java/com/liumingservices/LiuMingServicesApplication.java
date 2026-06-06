package com.liumingservices;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {"com"})
@MapperScan("com.liumingservices.ai.rag.mapper")
@SpringBootApplication
public class LiuMingServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiuMingServicesApplication.class, args);
        System.out.println("========================================");
        System.out.println(" LiuMing-backend项目启动成功 \n");
        System.out.println("========================================");

    }

}
