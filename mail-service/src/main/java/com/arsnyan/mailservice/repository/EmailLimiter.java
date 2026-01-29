package com.arsnyan.mailservice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class EmailLimiter {
    private static final String REDIS_KEY_PREFIX = "mail:";
    private static final String EMAILS_SENT_KEY = "emails_sent";

    private final RedisTemplate<String, Long> redisTemplate;

    @Value("${app.mail.daily-limit}")
    private int dailyLimit;

    /**
     * Adds 1 to the emails_sent counter
     * @return true if daily email limit was not reached
     */
    public boolean reserveLimit() {
        var key = REDIS_KEY_PREFIX + EMAILS_SENT_KEY;

        var currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == null) {
            return false;
        }

        if (currentCount == 1) {
            redisTemplate.expire(key, Duration.ofDays(1));
        }

        return currentCount < dailyLimit;
    }
}
