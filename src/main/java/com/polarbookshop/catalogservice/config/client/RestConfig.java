package com.polarbookshop.catalogservice.config.client;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Configuration
public class RestConfig {

    @Value("${http.connectTimeout:2000}")
    private int connectTimeoutMs;

    @Value("${http.readTimeout:10000}")
    private int readTimeoutMs;


    @Bean
    RestTemplate myRestClient(HttpComponentsClientHttpRequestFactory defaultHttpClientFactory) {
        return new RestTemplateBuilder()
                //.connectTimeout(Duration.ofMillis(connectTimeoutMs))
                .requestFactory(() -> defaultHttpClientFactory)
                .build();
    }

    @Bean
    HttpComponentsClientHttpRequestFactory defaultHttpClientFactory(HttpClient defaultHttpClient) {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(defaultHttpClient);
        //requestFactory.setConnectTimeout(connectTimeoutMs);
        return requestFactory;
    }


    @Bean(name = "defaultHttpClient")
    HttpClient defaultHttpClient() throws NoSuchAlgorithmException {
        SSLContext sslContext = SSLContext.getDefault();

        SSLConnectionSocketFactory connectionFactory =
                new SSLConnectionSocketFactory(sslContext, new DefaultHostnameVerifier());

        // Socket timeout
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(Timeout.ofMilliseconds(readTimeoutMs))
                .build();

        HttpClientConnectionManager httpClientConnectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultSocketConfig(socketConfig)
                .setMaxConnPerRoute(10)
                .setMaxConnTotal(20)
                .setSSLSocketFactory(connectionFactory)
                .build();

        return HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager).build();
    }
}
