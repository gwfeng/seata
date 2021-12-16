package com.lsh;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @author ：LiuShihao
 * @date ：Created in 2021/3/22 11:52 上午
 * @desc ：
 */
//发现服务
@EnableDiscoveryClient
@EnableFeignClients
@EnableEurekaClient
@MapperScan("com.lsh.dao.*")
@SpringBootApplication
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }
}
