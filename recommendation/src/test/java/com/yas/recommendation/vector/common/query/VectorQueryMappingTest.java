package com.yas.recommendation.vector.common.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import tools.jackson.databind.ObjectMapper;
import com.yas.recommendation.vector.common.document.BaseDocument;
import com.yas.recommendation.vector.common.document.DocumentMetadata;
import com.yas.recommendation.vector.common.formatter.DefaultDocumentFormatter;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.test.util.ReflectionTestUtils;

class VectorQueryMappingTest {

    @DocumentMetadata(docIdPrefix = "TEST", contentFormat = "{content}", documentFormatter = DefaultDocumentFormatter.class)
    private static class TestDocument extends BaseDocument {
    }

    private record ResultVm(String name) {
    }

    private static class TestVectorQuery extends VectorQuery<TestDocument, ResultVm> {
        TestVectorQuery() {
            super(TestDocument.class, ResultVm.class);
        }

        List<ResultVm> map(List<Document> documents) {
            return toResult(documents);
        }
    }

    @Test
    void toResult_skipsNullMetadata_andMapsToResultType() {
        TestVectorQuery query = new TestVectorQuery();
        ReflectionTestUtils.setField(query, "objectMapper", new ObjectMapper());

        Document docWithMetadata = new Document("content", Map.of("name", "alpha"));
        Document docWithoutMetadata = new Document("content", Map.of("name", "ignored"));
        ReflectionTestUtils.setField(docWithoutMetadata, "metadata", null);

        List<ResultVm> results = query.map(List.of(docWithMetadata, docWithoutMetadata));

        assertEquals(1, results.size());
        assertEquals("alpha", results.getFirst().name());
    }
}
