package com.xhz.yuncang;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching // 开启缓存
@MapperScan("com.xhz.yuncang.mapper")
public class YuncangApplication {

    public static void main(String[] args) {

        SpringApplication.run(YuncangApplication.class, args);
    }

}
