package com.yas.recommendation.vector.common.store;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.configuration.EmbeddingSearchConfiguration;
import com.yas.recommendation.vector.common.document.BaseDocument;
import com.yas.recommendation.vector.common.document.DefaultIdGenerator;
import com.yas.recommendation.vector.common.document.DocumentMetadata;
import com.yas.recommendation.vector.common.formatter.DefaultDocumentFormatter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.test.util.ReflectionTestUtils;

class SimpleVectorRepositoryTest {

    @DocumentMetadata(docIdPrefix = "TEST", contentFormat = "{name}|{description}", documentFormatter = DefaultDocumentFormatter.class)
    static class TestDocument extends BaseDocument {
    }

    private record TestEntity(String name, String description) {
    }

    private static class TestRepository extends SimpleVectorRepository<TestDocument, TestEntity> {

        private final Map<Long, TestEntity> data;

        TestRepository(VectorStore vectorStore, Map<Long, TestEntity> data) {
            super(TestDocument.class, vectorStore);
            this.data = data;
        }

        @Override
        public TestEntity getEntity(Long entityId) {
            return data.get(entityId);
        }
    }

    @Test
    void add_buildsDocumentWithMetadataAndContent() {
        VectorStore vectorStore = mock(VectorStore.class);
        Map<Long, TestEntity> data = Map.of(1L, new TestEntity("Laptop", "Fast"));
        TestRepository repository = new TestRepository(vectorStore, data);
        ReflectionTestUtils.setField(repository, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(repository, "embeddingSearchConfiguration",
                new EmbeddingSearchConfiguration(0.1, 5));

        repository.add(1L);

        ArgumentCaptor<List<Document>> docsCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(docsCaptor.capture());
        Document doc = docsCaptor.getValue().getFirst();
        assertEquals("Laptop|Fast", doc.getContent());
        assertNotNull(doc.getMetadata());
        assertEquals("TEST", doc.getMetadata().get(SimpleVectorRepository.TYPE_METADATA));
    }

    @Test
    void delete_usesGeneratedId() {
        VectorStore vectorStore = mock(VectorStore.class);
        Map<Long, TestEntity> data = Map.of(1L, new TestEntity("Laptop", "Fast"));
        TestRepository repository = new TestRepository(vectorStore, data);
        ReflectionTestUtils.setField(repository, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(repository, "embeddingSearchConfiguration",
                new EmbeddingSearchConfiguration(0.1, 5));

        repository.delete(1L);

        ArgumentCaptor<List<String>> idCaptor = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).delete(idCaptor.capture());
        String expected = new DefaultIdGenerator("TEST", 1L).generateId();
        assertEquals(expected, idCaptor.getValue().getFirst());
    }

    @Test
    void search_buildsSearchRequest_andMapsDocuments() {
        VectorStore vectorStore = mock(VectorStore.class);
        Map<Long, TestEntity> data = Map.of(1L, new TestEntity("Laptop", "Fast"));
        TestRepository repository = new TestRepository(vectorStore, data);
        ReflectionTestUtils.setField(repository, "objectMapper", new ObjectMapper());
        ReflectionTestUtils.setField(repository, "embeddingSearchConfiguration",
                new EmbeddingSearchConfiguration(0.2, 2));

        Document returned = new Document("result-content", Map.of("id", 2L));
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(returned));

        List<TestDocument> result = repository.search(1L);

        assertEquals(1, result.size());
        assertEquals("result-content", result.getFirst().getContent());
    }
}
