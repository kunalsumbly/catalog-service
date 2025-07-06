
# Making Redis Connection Refreshable with @RefreshScope

Yes, you need to add the `@RefreshScope` annotation to make your Redis connection refreshable and dynamically change at runtime when configuration properties like the password are updated in the Config Server.

## Current Setup

Currently, your application:
1. Uses Spring Boot's auto-configuration for Redis
2. Gets the Redis password from Config Server via `${REDIS_PASSWORD}` in application.properties
3. Has a `ConfigRefresher` class that calls the `/actuator/refresh` endpoint every minute

However, this setup doesn't automatically recreate the Redis connection when the password changes. The environment properties are refreshed, but the Redis connection bean continues to use the old password.

## Solution: Add @RefreshScope to Redis Configuration

To make the Redis connection refreshable, you need to create an explicit Redis configuration class with `@RefreshScope`:

```java
package com.polarbookshop.catalogservice.config;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;
    
    @Value("${spring.data.redis.port}")
    private int redisPort;
    
    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    @RefreshScope
    public RedisConnectionFactory redisConnectionFactory() {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfig.setPassword(redisPassword);
        return new LettuceConnectionFactory(redisConfig);
    }

    @Bean(name = "stringRedisTemplate")
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setHashValueSerializer(stringRedisSerializer);
        
        template.afterPropertiesSet();
        return template;
    }
}
```

## How This Works

1. The `@RefreshScope` annotation on the `redisConnectionFactory()` method tells Spring to recreate this bean when configuration properties change
2. When your `ConfigRefresher` calls the `/actuator/refresh` endpoint, Spring will:
    - Update the environment properties (including `spring.data.redis.password`)
    - Destroy and recreate any beans marked with `@RefreshScope` that depend on those properties
    - The new Redis connection will use the updated password

3. Your `HomeController` will automatically use the new connection since it depends on the `RedisTemplate` bean

## Important Notes

1. Only the `RedisConnectionFactory` needs `@RefreshScope` since it's the bean that directly uses the password
2. The `RedisTemplate` doesn't need `@RefreshScope` because it depends on the connection factory
3. This approach works with both plain text and encrypted passwords in the Config Server
4. No changes are needed to your existing `ConfigRefresher` class

With this configuration, your Redis connection will automatically use the new password whenever it changes in the Config Server, without requiring an application restart.