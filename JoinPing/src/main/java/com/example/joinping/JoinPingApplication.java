package com.example.joinping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.example.joinping.mapper")
@EnableScheduling
@EnableAspectJAutoProxy
public class JoinPingApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(JoinPingApplication.class, args);
    }
    
}