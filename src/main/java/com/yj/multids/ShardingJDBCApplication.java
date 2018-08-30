package com.yj.multids;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration(exclude = { io.shardingjdbc.spring.boot.SpringBootConfiguration.class })
public class ShardingJDBCApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ShardingJDBCApplication.class, args);
	}
	
}
