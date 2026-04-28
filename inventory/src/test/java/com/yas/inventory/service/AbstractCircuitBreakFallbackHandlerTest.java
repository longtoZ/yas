package com.yas.inventory.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class AbstractCircuitBreakFallbackHandlerTest {

    private static class TestCircuitBreakFallbackHandler extends AbstractCircuitBreakFallbackHandler {
        public void testHandleBodilessFallback(Throwable throwable) throws Throwable {
            handleBodilessFallback(throwable);
        }

        public <T> T testHandleTypedFallback(Throwable throwable) throws Throwable {
            return handleTypedFallback(throwable);
        }
    }

    @Test
    void testHandleBodilessFallback_withThrowable_rethrowsException() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        RuntimeException originalException = new RuntimeException("Circuit breaker error");

        assertThrows(RuntimeException.class, () -> {
            handler.testHandleBodilessFallback(originalException);
        });
    }

    @Test
    void testHandleBodilessFallback_withNullPointerException_rethrowsException() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        NullPointerException exception = new NullPointerException("Null pointer occurred");

        assertThrows(NullPointerException.class, () -> {
            handler.testHandleBodilessFallback(exception);
        });
    }

    @Test
    void testHandleTypedFallback_withThrowable_rethrowsAndReturnsNull() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        RuntimeException originalException = new RuntimeException("Typed fallback error");

        assertThrows(RuntimeException.class, () -> {
            handler.testHandleTypedFallback(originalException);
        });
    }

    @Test
    void testHandleTypedFallback_withIOException_rethrowsException() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        java.io.IOException ioException = new java.io.IOException("IO error occurred");

        assertThrows(java.io.IOException.class, () -> {
            handler.testHandleTypedFallback(ioException);
        });
    }

    @Test
    void testHandleBodilessFallback_withDifferentExceptionTypes_preservesExceptionType() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();

        IllegalArgumentException illegalArgException = new IllegalArgumentException("Invalid argument");
        assertThrows(IllegalArgumentException.class, () -> {
            handler.testHandleBodilessFallback(illegalArgException);
        });

        IllegalStateException illegalStateException = new IllegalStateException("Invalid state");
        assertThrows(IllegalStateException.class, () -> {
            handler.testHandleBodilessFallback(illegalStateException);
        });
    }

    @Test
    void testHandleTypedFallback_withCheckedException_rethrowsException() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        Exception checkedException = new Exception("Checked exception");

        assertThrows(Exception.class, () -> {
            handler.testHandleTypedFallback(checkedException);
        });
    }

    @Test
    void testHandleBodilessFallback_withExceptionMessage_preservesMessage() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        String expectedMessage = "Specific circuit breaker failure message";
        RuntimeException exception = new RuntimeException(expectedMessage);

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            handler.testHandleBodilessFallback(exception);
        });

        assert thrownException.getMessage().contains(expectedMessage) || thrownException.getMessage() == null;
    }

    @Test
    void testHandleTypedFallback_withCause_preservesThrowableChain() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        Exception cause = new Exception("Root cause");
        RuntimeException exception = new RuntimeException("Outer exception", cause);

        RuntimeException thrownException = assertThrows(RuntimeException.class, () -> {
            handler.testHandleTypedFallback(exception);
        });

        assert thrownException.getCause() != null;
    }

    @Test
    void testHandleBodilessFallback_multipleInvocations_eachRethrows() {
        TestCircuitBreakFallbackHandler handler = new TestCircuitBreakFallbackHandler();
        RuntimeException exception1 = new RuntimeException("First error");
        RuntimeException exception2 = new RuntimeException("Second error");

        assertThrows(RuntimeException.class, () -> handler.testHandleBodilessFallback(exception1));
        assertThrows(RuntimeException.class, () -> handler.testHandleBodilessFallback(exception2));
    }
}
