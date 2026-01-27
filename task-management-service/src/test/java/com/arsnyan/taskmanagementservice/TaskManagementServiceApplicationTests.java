package com.arsnyan.taskmanagementservice;

import com.arsnyan.taskmanagementservice.config.TestSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import({TestcontainersConfiguration.class, TestSecurityConfiguration.class})
@SpringBootTest
class TaskManagementServiceApplicationTests {
    @Test
    void contextLoads() {
    }
}
