package com.yas.recommendation.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.PgVectorStore;

class VectorStorePropertiesTest {

    @Test
    void settersAndGetters_work() {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimensions(512);
        properties.setDistanceType(PgVectorStore.PgDistanceType.values()[0]);
        properties.setIndexType(PgVectorStore.PgIndexType.values()[0]);
        properties.setInitializeSchema(true);

        assertEquals(512, properties.getDimensions());
        assertEquals(PgVectorStore.PgDistanceType.values()[0], properties.getDistanceType());
        assertEquals(PgVectorStore.PgIndexType.values()[0], properties.getIndexType());
        assertTrue(properties.isInitializeSchema());
    }
}
