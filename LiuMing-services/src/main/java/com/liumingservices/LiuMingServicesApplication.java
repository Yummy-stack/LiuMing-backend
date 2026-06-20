package com.liumingservices;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jAutoConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@ComponentScan(basePackages = {"com"})
@MapperScan("com.liumingservices.mapper")
@SpringBootApplication(exclude = {
        Knife4jAutoConfiguration.class
})
public class LiuMingServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiuMingServicesApplication.class, args);

        System.out.println("\n========================================");
        System.out.println("LiuMing-backend项目启动成功");
        System.out.println("接口文档地址: http://localhost:8080/api/doc.html");
        System.out.println("========================================");

    }

}
