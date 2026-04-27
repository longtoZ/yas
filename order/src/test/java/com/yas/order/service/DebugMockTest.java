package com.yas.order.service;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import org.springframework.web.client.RestClient;

public class DebugMockTest {
    @Test
    public void test() {
        RestClient.RequestBodySpec spec = mock(RestClient.RequestBodySpec.class);
        when(spec.body(any())).thenReturn(spec);
        System.out.println("Result: " + spec.body(new java.util.ArrayList<>()));
    }
}
