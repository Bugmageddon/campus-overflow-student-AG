package com.campusoverflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * CampusOverflow 启动类——模块化单体的“组装点”。
 * 组件扫描覆盖 com.campusoverflow 下的四个限界上下文；各上下文之间的边界由 Maven 依赖与 ArchUnit 共同守护。
 */
@SpringBootApplication
@EnableScheduling
public class CampusOverflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusOverflowApplication.class, args);
    }
}
