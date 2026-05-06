package com.ucto.backend.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.mock;

/**
 * Test configuration that mocks infrastructure dependencies,
 * allowing @SpringBootTest to load without a real Redis server or SMTP server.
 */
@TestConfiguration
public class TestRedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }

    @SuppressWarnings("unchecked")
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate() {
        return (RedisTemplate<String, Object>) mock(RedisTemplate.class);
    }

    @Bean
    @Primary
    public StringRedisTemplate stringRedisTemplate() {
        return mock(StringRedisTemplate.class);
    }

    @Bean
    @Primary
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        return mock(RedisMessageListenerContainer.class);
    }

    @Bean
    @Primary
    public JavaMailSender javaMailSender() {
        return mock(JavaMailSender.class);
    }
}
