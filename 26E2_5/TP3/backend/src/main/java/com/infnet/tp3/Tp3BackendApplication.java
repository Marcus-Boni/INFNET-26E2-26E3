package com.infnet.tp3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
@EnableFeignClients
public class Tp3BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(Tp3BackendApplication.class, args);
    }
}
