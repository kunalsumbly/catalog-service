package com.polarbookshop.catalogservice.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RestConfig {

    @Value("${http.connectTimeout:2000}")
    private int connectTimeoutMs;

    @Value("${http.readTimeout:10000}")
    private int readTimeoutMs;

    @Value("${http.socketTimeout:10000}")
    private int socketTimeoutMs;

    @Bean
    RestTemplate myRestClient() {
        return new RestTemplateBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .readTimeout(Duration.ofMillis(readTimeoutMs))
                .requestFactory(this::clientHttpRequestFactory)
                .build();
    }

    @Bean
    SimpleClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(socketTimeoutMs); // SimpleClientHttpRequestFactory uses readTimeout for socket timeout
        return factory;
    }
}
