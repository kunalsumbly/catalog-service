package com.polarbookshop.catalogservice.config.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

@Configuration
@Slf4j
public class ClientDecryptionConfiguration {

    @Value("${encrypt.key}")
    private String encryptionKey;

    @Value("${encrypt.salt}")
    private String encryptionSalt;

    @Bean
    @Primary
    public TextEncryptor textEncryptor() {
        log.info("Configuring client-side decryption with matching encryption parameters");
        return Encryptors.text(encryptionKey, encryptionSalt);
    }
}
