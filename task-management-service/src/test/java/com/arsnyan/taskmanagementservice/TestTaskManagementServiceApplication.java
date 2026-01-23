package com.arsnyan.taskmanagementservice;

import org.springframework.boot.SpringApplication;

public class TestTaskManagementServiceApplication {
    public static void main(String[] args) {
        SpringApplication.from(TaskManagementServiceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }
}
