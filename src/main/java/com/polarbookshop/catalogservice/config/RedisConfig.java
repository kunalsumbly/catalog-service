package com.polarbookshop.catalogservice.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.polarbookshop.catalogservice.domain.model.Book;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Book> bookRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, Book> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use StringRedisSerializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        
        // Use Jackson2JsonRedisSerializer for values
        Jackson2JsonRedisSerializer<Book> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, Book.class);
        template.setValueSerializer(serializer);
        
        return template;
    }
    
    @Bean
    public RedisTemplate<String, List<Book>> bookListRedisTemplate(RedisConnectionFactory connectionFactory, ObjectMapper objectMapper) {
        RedisTemplate<String, List<Book>> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use StringRedisSerializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        
        // Create a custom serializer for List<Book>
        Jackson2JsonRedisSerializer<List<Book>> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, 
            objectMapper.getTypeFactory().constructCollectionType(List.class, Book.class));
        template.setValueSerializer(serializer);
        
        return template;
    }
}