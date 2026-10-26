package com.finance.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


// TODO: Add configuration file to set DB and auth
@SpringBootApplication
@EnableFeignClients(basePackages = "com.finance.account.infraestructure.driver_adapters")
public class AccountServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
