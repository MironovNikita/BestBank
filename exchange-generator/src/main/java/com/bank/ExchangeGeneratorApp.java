package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class ExchangeGeneratorApp {
    public static void main(String[] args) {
        SpringApplication.run(ExchangeGeneratorApp.class, args);
    }
}