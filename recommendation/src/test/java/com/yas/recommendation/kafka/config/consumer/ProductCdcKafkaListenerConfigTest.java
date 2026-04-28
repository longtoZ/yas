package com.yas.recommendation.kafka.config.consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

class ProductCdcKafkaListenerConfigTest {

    @Test
    void listenerContainerFactory_isCreated() {
        ProductCdcKafkaListenerConfig config = new ProductCdcKafkaListenerConfig(new KafkaProperties());

        ConcurrentKafkaListenerContainerFactory<?, ?> factory = config.listenerContainerFactory();

        assertNotNull(factory);
    }
}
