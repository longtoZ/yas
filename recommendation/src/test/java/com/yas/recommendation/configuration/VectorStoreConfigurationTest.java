package com.yas.recommendation.configuration;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.PgVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.mockito.Mockito;

class VectorStoreConfigurationTest {

    @Test
    void vectorStore_createsPgVectorStore() {
        VectorStoreProperties properties = new VectorStoreProperties();
        properties.setDimensions(1536);
        properties.setDistanceType(PgVectorStore.PgDistanceType.values()[0]);
        properties.setIndexType(PgVectorStore.PgIndexType.values()[0]);
        properties.setInitializeSchema(false);

        VectorStoreConfiguration config = new VectorStoreConfiguration(properties);
        VectorStore store = config.vectorStore(Mockito.mock(JdbcTemplate.class), Mockito.mock(EmbeddingModel.class));

        assertNotNull(store);
    }
}
