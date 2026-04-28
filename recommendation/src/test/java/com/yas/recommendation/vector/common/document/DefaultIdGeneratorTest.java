package com.yas.recommendation.vector.common.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class DefaultIdGeneratorTest {

    @Test
    void generateId_usesPrefixAndIdentity() {
        DefaultIdGenerator generator = new DefaultIdGenerator("TEST", 42L);

        String id = generator.generateId();

        String expected = UUID.nameUUIDFromBytes("TEST-42".getBytes()).toString();
        assertEquals(expected, id);
    }
}
