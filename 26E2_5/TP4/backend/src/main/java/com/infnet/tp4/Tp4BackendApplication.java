package com.infnet.tp4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class Tp4BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Tp4BackendApplication.class, args);
    }
}

