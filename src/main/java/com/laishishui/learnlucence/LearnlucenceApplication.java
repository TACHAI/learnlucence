package com.laishishui.learnlucence;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//@MapperScan("com.laishishui.learnlucence.dao")
public class LearnlucenceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearnlucenceApplication.class, args);
    }

}
