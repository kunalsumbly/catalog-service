package com.polarbookshop.catalogservice.controller;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.client5.http.ssl.DefaultHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.SSLContext;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;

@RestController
public class BlackHoleDemoController {

    // Inject via application.properties:
    // http.connectTimeout=2000
    // http.readTimeout=10000

    private final RestTemplate restTemplate;

    public BlackHoleDemoController(@Qualifier("myRestClient") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

//    public BlackHoleDemoController() {
//        // Build Apache HttpClient 5 with RequestConfig (but NO default responseTimeout)
//        RequestConfig reqCfg = RequestConfig.custom()
//            .setConnectTimeout(Timeout.ofMilliseconds(connectTimeoutMs))
//            // Omitting .setResponseTimeout(...) to replicate default 3 min behavior
//            .build();
//
//        CloseableHttpClient httpClient = HttpClients.custom()
//            .setDefaultRequestConfig(reqCfg)
//            .build();
//
//        HttpComponentsClientHttpRequestFactory factory =
//            new HttpComponentsClientHttpRequestFactory(httpClient);
//        factory.setConnectTimeout(connectTimeoutMs);
//        factory.setReadTimeout(readTimeoutMs);
//
//        this.restTemplate = new RestTemplate(factory);
//    }

    @GetMapping("/test-blackhole")
    public ResponseEntity<String> testBlackHole() {
        String url = "http://13.55.108.129/api/kunal/helloworld";
        // To simulate a black-hole, block or drop packets to httpbin.org,
        // e.g., via local firewall: sudo iptables -A OUTPUT -p tcp --dport 443 -j DROP

        // The call will hang until the OS's SYN retries exhaust (~2+ minutes)
        return restTemplate.getForEntity(url, String.class);
    }

}
