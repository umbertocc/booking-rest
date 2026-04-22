package com.example.mail;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example.mail")
public class DemoMailApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoMailApplication.class, args);
    }
}
