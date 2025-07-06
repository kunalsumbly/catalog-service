package com.polarbookshop.catalogservice.config.redis;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@RefreshScope
@Data
public class RedisProperties {
    private String host;
    private int port;
    private String password;
    
    // getters and setters
}

//@Configuration
//public class RedisConfig {
//
//    @Bean
//    @RefreshScope
//    public RedisConnectionFactory redisConnectionFactory(RedisProperties redisProperties) {
//        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration(
//            redisProperties.getHost(),
//            redisProperties.getPort()
//        );
//        redisConfig.setPassword(redisProperties.getPassword());
//        return new LettuceConnectionFactory(redisConfig);
//    }
//}
