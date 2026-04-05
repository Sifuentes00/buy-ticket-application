package com.matvey.cinema.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.url:http://localhost:8080}")
    private String serverUrl;
    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("cinema-app")                          // или другой, если хочешь админский доступ
                .username("admin")                        // админ Keycloak
                .password("admin")
                .clientId("admin-cli")                    // встроенный клиент для админки
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}
