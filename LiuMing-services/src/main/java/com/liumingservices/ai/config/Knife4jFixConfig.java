package com.liumingservices.ai.config;

import com.github.xiaoymin.knife4j.spring.configuration.Knife4jProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 专门用于修复 Spring Boot 3.2+ 与 Knife4j 产生的 Bean 冲突问题
 */
@Configuration
public class Knife4jFixConfig {

    @Bean
    @Primary // 核心救星：告诉 Spring，一旦遇到多个 Knife4jProperties，无脑选我这个！
    @ConfigurationProperties(prefix = "knife4j")
    public Knife4jProperties primaryKnife4jProperties() {
        return new Knife4jProperties();
    }
}