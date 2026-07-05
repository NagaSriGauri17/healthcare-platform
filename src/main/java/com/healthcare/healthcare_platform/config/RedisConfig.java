package com.healthcare.healthcare_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.net.URI;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory() {
        String redisUrl = System.getenv("REDIS_URL");
        if (redisUrl == null || redisUrl.isEmpty()) {
            redisUrl = "redis://localhost:6379";
        }

        System.out.println("=== RedisConfig: Building connection factory with URL: " + redisUrl + " ===");

        try {
            URI uri = new URI(redisUrl);
            System.out.println("=== Parsed host: " + uri.getHost() + ", port: " + uri.getPort() + " ===");

            RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
            config.setHostName(uri.getHost());
            config.setPort(uri.getPort());

            String userInfo = uri.getUserInfo();
            if (userInfo != null && userInfo.contains(":")) {
                String password = userInfo.substring(userInfo.indexOf(":") + 1);
                if (!password.isEmpty()) {
                    config.setPassword(password);
                }
            }

            return new LettuceConnectionFactory(config);
        } catch (Exception e) {
            System.out.println("=== RedisConfig ERROR: " + e.getMessage() + " ===");
            throw new RuntimeException("Failed to parse Redis URL: " + redisUrl, e);
        }
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}