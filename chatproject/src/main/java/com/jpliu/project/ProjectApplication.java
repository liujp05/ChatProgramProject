package com.jpliu.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import tk.mybatis.spring.annotation.MapperScan;

import javax.swing.*;

/**
 *
 */
@SpringBootApplication
//扫描mybites的包路径
@MapperScan(basePackages = "com.jpliu.project.mapper")
@ComponentScan(basePackages = {"com.jpliu", "org.n3r.idworker"})
public class ProjectApplication {

	@Bean
	public SpringUtil getSpringUtil() {
		return new SpringUtil();
	}

	public static void main(String[] args) {
		SpringApplication.run(ProjectApplication.class, args);
	}

}

