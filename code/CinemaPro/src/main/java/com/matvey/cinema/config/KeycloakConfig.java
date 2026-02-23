package com.matvey.cinema.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Bean
    public Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl("http://localhost:8080")       // твой Keycloak URL
                .realm("cinema-app")                          // или другой, если хочешь админский доступ
                .username("admin")                        // админ Keycloak
                .password("admin")
                .clientId("admin-cli")                    // встроенный клиент для админки
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}
