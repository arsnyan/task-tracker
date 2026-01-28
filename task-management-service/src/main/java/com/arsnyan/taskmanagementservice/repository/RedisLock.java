package com.arsnyan.taskmanagementservice.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisLock {
    private final StringRedisTemplate redisTemplate;

    public boolean tryLock(String lockKey, Duration ttl) {
        var acquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, instanceId(), ttl);

        if (Boolean.TRUE.equals(acquired)) {
            log.debug("Acquired lock: {}", lockKey);
            return true;
        }

        log.debug("Lock already held: {}", lockKey);
        return false;
    }

    public void release(String lockKey) {
        var holder = redisTemplate.opsForValue().get(lockKey);

        if (instanceId().equals(holder)) {
            redisTemplate.delete(lockKey);
            log.debug("Released lock: {}", lockKey);
        }
    }

    private String instanceId() {
        return ProcessHandle.current().pid() + "@" + getHostname();
    }

    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
