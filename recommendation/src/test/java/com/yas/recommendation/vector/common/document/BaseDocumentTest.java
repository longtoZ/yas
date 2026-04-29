package com.yas.recommendation.vector.common.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yas.recommendation.vector.common.formatter.DefaultDocumentFormatter;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.id.IdGenerator;

class BaseDocumentTest {

    @DocumentMetadata(docIdPrefix = "TEST", contentFormat = "{content}", documentFormatter = DefaultDocumentFormatter.class)
    private static class TestDocument extends BaseDocument {
    }

    private static class NoMetadataDocument extends BaseDocument {
    }

    @Test
    void toDocument_buildsDocumentWithFormatter() {
        TestDocument doc = new TestDocument();
        doc.setContent("payload");
        doc.setMetadata(Map.of("id", 1L));
        IdGenerator idGenerator = new DefaultIdGenerator("TEST", 1L);

        Document result = doc.toDocument(idGenerator);

        assertEquals("payload", result.getContent());
        assertNotNull(result.getMetadata());
        assertNotNull(result.getContentFormatter());
    }

    @Test
    void toDocument_requiresMetadataAnnotation() {
        NoMetadataDocument doc = new NoMetadataDocument();
        doc.setContent("payload");
        doc.setMetadata(Map.of("id", 1L));

        assertThrows(IllegalArgumentException.class, () -> doc.toDocument(new DefaultIdGenerator("TEST", 1L)));
    }

    @Test
    void toDocument_requiresContentAndMetadata() {
        TestDocument doc = new TestDocument();

        assertThrows(IllegalArgumentException.class, () -> doc.toDocument(new DefaultIdGenerator("TEST", 1L)));

        doc.setContent("payload");
        assertThrows(IllegalArgumentException.class, () -> doc.toDocument(new DefaultIdGenerator("TEST", 1L)));
    }
}
