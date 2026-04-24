package com.yas.recommendation.kafka.config.consumer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

class AppKafkaListenerConfigurerTest {

    @Test
    void configureKafkaListeners_setsValidator() {
        LocalValidatorFactoryBean validator = mock(LocalValidatorFactoryBean.class);
        KafkaListenerEndpointRegistrar registrar = mock(KafkaListenerEndpointRegistrar.class);

        AppKafkaListenerConfigurer configurer = new AppKafkaListenerConfigurer(validator);
        configurer.configureKafkaListeners(registrar);

        verify(registrar).setValidator(validator);
    }
}
