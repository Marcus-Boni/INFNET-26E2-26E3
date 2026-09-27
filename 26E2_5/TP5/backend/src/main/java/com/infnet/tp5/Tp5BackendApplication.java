package com.infnet.tp5;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class Tp5BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Tp5BackendApplication.class, args);
    }
}


