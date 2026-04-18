package com.liumingservices;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

@ComponentScan(basePackages = {"com"})
@SpringBootApplication
public class LiuMingServicesApplication {

    public static void main(String[] args) {
        SpringApplication.run(LiuMingServicesApplication.class, args);
        System.out.println("\n ----------------- 项目启动成功 ----------------- \n");
    }

}
