package com.pack.material;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.pack.material.mapper")
@EnableCaching
@EnableScheduling
public class PackMaterialApplication {

    public static void main(String[] args) {
        SpringApplication.run(PackMaterialApplication.class, args);
        System.out.println("========================================");
        System.out.println("  包装物料管理系统启动成功！");
        System.out.println("  接口地址: http://localhost:8080/api");
        System.out.println("========================================");
    }
}
