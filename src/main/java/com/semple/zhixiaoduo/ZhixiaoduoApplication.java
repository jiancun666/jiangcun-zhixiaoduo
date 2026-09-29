package com.semple.zhixiaoduo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.semple.zhixiaoduo.mapper")
@SpringBootApplication
public class ZhixiaoduoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhixiaoduoApplication.class, args);
    }
}
