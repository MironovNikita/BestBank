package com.bank;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class TransfersServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(TransfersServiceApp.class, args);
    }
}