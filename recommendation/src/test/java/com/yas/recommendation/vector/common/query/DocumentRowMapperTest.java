package com.yas.recommendation.vector.common.query;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import tools.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

class DocumentRowMapperTest {

    @Test
    void mapRow_readsDocumentFields() throws Exception {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(DocumentRowMapper.ID)).thenReturn("doc-1");
        when(rs.getString(DocumentRowMapper.CONTENT)).thenReturn("content");
        when(rs.getObject(DocumentRowMapper.METADATA)).thenReturn("{\"name\":\"alpha\"}");

        DocumentRowMapper mapper = new DocumentRowMapper(new ObjectMapper());
        Document doc = mapper.mapRow(rs, 1);

        assertEquals("doc-1", doc.getId());
        assertEquals("content", doc.getContent());
        assertNotNull(doc.getMetadata());
        assertEquals("alpha", doc.getMetadata().get("name"));
    }
}
