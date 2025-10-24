package com.matvey.cinema.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.Module; // Импортируем Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
// Импорт модуля для Hibernate/JPA в среде Jakarta Persistence
// Убедитесь, что вы добавили зависимость jackson-datatype-hibernate5-jakarta в pom.xml
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    private static final Logger logger = LoggerFactory.getLogger(JacksonConfig.class);

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        logger.info("Configuring ObjectMapper with custom settings and modules.");

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);

        objectMapper.coercionConfigFor(String.class)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Array, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.Fail);

        objectMapper.registerModule(new JavaTimeModule());

        Hibernate5JakartaModule hibernateModule = new Hibernate5JakartaModule();


        objectMapper.registerModule(hibernateModule);
        logger.info("Hibernate5JakartaModule registered with ObjectMapper.");
        // =========================================================

        return objectMapper;
    }

}
