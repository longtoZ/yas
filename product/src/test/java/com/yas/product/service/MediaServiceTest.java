package com.yas.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.commonlibrary.config.ServiceUrlConfig;
import com.yas.product.viewmodel.NoFileMediaVm;
import java.net.URI;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.client.RestClient;

class MediaServiceTest {

    private RestClient restClient;
    private ServiceUrlConfig serviceUrlConfig;
    private MediaService mediaService;

    @BeforeEach
    void setUp() {
        restClient = mock(RestClient.class);
        serviceUrlConfig = new ServiceUrlConfig("http://media-service", "http://product-service");
        mediaService = new MediaService(restClient, serviceUrlConfig);

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim("sub", "user")
                .build();
        Authentication auth = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getMedia_whenIdNull_returnsDefaultVm() {
        NoFileMediaVm result = mediaService.getMedia(null);

        assertNotNull(result);
        assertNull(result.id());
        assertEquals("", result.caption());
        assertEquals("", result.fileName());
        assertEquals("", result.mediaType());
        assertEquals("", result.url());
    }

    @Test
    void getMedia_whenIdProvided_usesRestClient() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec getSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        NoFileMediaVm expected = new NoFileMediaVm(1L, "", "", "", "url");

        when(restClient.get()).thenReturn(getSpec);
        when(getSpec.uri(any(URI.class))).thenReturn(getSpec);
        when(getSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NoFileMediaVm.class)).thenReturn(expected);

        NoFileMediaVm result = mediaService.getMedia(1L);

        assertEquals(expected, result);
        verify(getSpec).uri(any(URI.class));
    }

    @Test
    void saveFile_setsBearerTokenAndReturnsResponse() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestBodyUriSpec postSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        NoFileMediaVm expected = new NoFileMediaVm(2L, "", "", "", "url");

        when(restClient.post()).thenReturn(postSpec);
        when(postSpec.uri(any(URI.class))).thenReturn(postSpec);
        when(postSpec.contentType(MediaType.MULTIPART_FORM_DATA)).thenReturn(postSpec);
        when(postSpec.headers(any())).thenReturn(postSpec);
        when(postSpec.body(any(org.springframework.util.MultiValueMap.class))).thenReturn(postSpec);
        when(postSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(NoFileMediaVm.class)).thenReturn(expected);

        MockMultipartFile file = new MockMultipartFile("file", "file.txt", "text/plain", "data".getBytes());

        NoFileMediaVm result = mediaService.saveFile(file, "cap", "override");

        assertEquals(expected, result);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass((Class) Consumer.class);
        verify(postSpec).headers(headersCaptor.capture());
        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertEquals("Bearer test-token", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }

    @Test
    void removeMedia_setsBearerToken() {
        @SuppressWarnings("rawtypes")
        RestClient.RequestHeadersUriSpec deleteSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.delete()).thenReturn(deleteSpec);
        when(deleteSpec.uri(any(URI.class))).thenReturn(deleteSpec);
        when(deleteSpec.headers(any())).thenReturn(deleteSpec);
        when(deleteSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(eq(Void.class))).thenReturn(null);

        mediaService.removeMedia(3L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Consumer<HttpHeaders>> headersCaptor = ArgumentCaptor.forClass((Class) Consumer.class);
        verify(deleteSpec).headers(headersCaptor.capture());
        HttpHeaders headers = new HttpHeaders();
        headersCaptor.getValue().accept(headers);
        assertEquals("Bearer test-token", headers.getFirst(HttpHeaders.AUTHORIZATION));
    }
}
