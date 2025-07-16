package com.polarbookshop.catalogservice.config.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@Slf4j
public class ClientDecryptionConfiguration {

    @Bean
    @Primary
    public TextEncryptor textEncryptor(
            @Value("${encrypt.key}") String encryptionKey,
            @Value("${encrypt.salt}") String encryptionSalt) {

        log.info("Configuring client-side decryption with key: {} and salt: {}", encryptionKey, encryptionSalt);
        return Encryptors.text(encryptionKey, encryptionSalt);
    }
}
