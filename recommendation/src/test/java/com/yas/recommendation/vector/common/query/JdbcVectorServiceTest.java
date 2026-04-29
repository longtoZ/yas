package com.yas.recommendation.vector.common.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.configuration.EmbeddingSearchConfiguration;
import com.yas.recommendation.vector.common.document.BaseDocument;
import com.yas.recommendation.vector.common.document.DocumentMetadata;
import com.yas.recommendation.vector.common.formatter.DefaultDocumentFormatter;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.test.util.ReflectionTestUtils;

class JdbcVectorServiceTest {

    @DocumentMetadata(docIdPrefix = "TEST", contentFormat = "{content}", documentFormatter = DefaultDocumentFormatter.class)
    private static class TestDocument extends BaseDocument {
    }

    @Test
    void similarityProduct_usesConfiguredTableName_andReturnsResults() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        EmbeddingSearchConfiguration embeddingSearchConfiguration = mock(EmbeddingSearchConfiguration.class);
        when(embeddingSearchConfiguration.similarityThreshold()).thenReturn(0.1);
        when(embeddingSearchConfiguration.topK()).thenReturn(3);

        List<Document> expected = List.of(new Document("content", java.util.Map.of("name", "alpha")));
        when(jdbcTemplate.query(anyString(), any(PreparedStatementSetter.class), any(DocumentRowMapper.class)))
                .thenReturn(expected);

        JdbcVectorService service = new JdbcVectorService(jdbcTemplate, new ObjectMapper(),
                embeddingSearchConfiguration);
        ReflectionTestUtils.setField(service, "vectorTableName", "vector_store_custom");

        List<Document> result = service.similarityProduct(10L, TestDocument.class);

        assertEquals(expected, result);
        ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
        verify(jdbcTemplate)
                .query(sqlCaptor.capture(), any(PreparedStatementSetter.class), any(DocumentRowMapper.class));
        assertTrue(sqlCaptor.getValue().contains("vector_store_custom"));
    }
}
