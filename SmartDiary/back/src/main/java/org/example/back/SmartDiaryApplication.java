package org.example.back;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "org.example.back.mapper")
public class SmartDiaryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartDiaryApplication.class, args);
    }

}