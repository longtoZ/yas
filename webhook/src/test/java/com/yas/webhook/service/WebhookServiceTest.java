package com.yas.webhook.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.yas.commonlibrary.exception.NotFoundException;
import com.yas.webhook.integration.api.WebhookApi;
import com.yas.webhook.model.WebhookEventNotification;
import com.yas.webhook.model.dto.WebhookEventNotificationDto;
import com.yas.webhook.model.mapper.WebhookMapper;
import com.yas.webhook.repository.EventRepository;
import com.yas.webhook.repository.WebhookEventNotificationRepository;
import com.yas.webhook.repository.WebhookEventRepository;
import com.yas.webhook.repository.WebhookRepository;

@ExtendWith(MockitoExtension.class)
class WebhookServiceTest {

    @Mock
    WebhookRepository webhookRepository;
    @SuppressWarnings("unused")
    @Mock
    EventRepository eventRepository;
    @Mock
    WebhookEventRepository webhookEventRepository;
    @Mock
    WebhookEventNotificationRepository webhookEventNotificationRepository;
    @SuppressWarnings("unused")
    @Mock
    WebhookMapper webhookMapper;
    @Mock
    WebhookApi webHookApi;

    @InjectMocks
    WebhookService webhookService;

    @Test
    void test_notifyToWebhook_ShouldNotException() {

        WebhookEventNotificationDto notificationDto = WebhookEventNotificationDto
                .builder()
                .notificationId(1L)
                .url("")
                .secret("")
                .build();

        WebhookEventNotification notification = new WebhookEventNotification();
        when(webhookEventNotificationRepository.findById(notificationDto.getNotificationId()))
                .thenReturn(Optional.of(notification));

        webhookService.notifyToWebhook(notificationDto);

        verify(webhookEventNotificationRepository).save(notification);
        verify(webHookApi).notify(notificationDto.getUrl(), notificationDto.getSecret(), notificationDto.getPayload());
    }

    @Test
    void test_notifyToWebhook_whenNotificationNotFound_throwNoSuchElementException() {
        WebhookEventNotificationDto notificationDto = WebhookEventNotificationDto
                .builder()
                .notificationId(99L)
                .url("http://example.com")
                .secret("secret")
                .build();

        when(webhookEventNotificationRepository.findById(notificationDto.getNotificationId()))
                .thenReturn(Optional.empty());

        java.util.NoSuchElementException exception = assertThrows(java.util.NoSuchElementException.class,
                () -> webhookService.notifyToWebhook(notificationDto));
        assertNotNull(exception);
        verify(webHookApi).notify(notificationDto.getUrl(), notificationDto.getSecret(), notificationDto.getPayload());
        verify(webhookEventNotificationRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void test_delete_whenWebhookExists_deleteWebhookAndEvents() {
        Long webhookId = 1L;
        when(webhookRepository.existsById(webhookId)).thenReturn(true);

        webhookService.delete(webhookId);

        verify(webhookEventRepository).deleteByWebhookId(webhookId);
        verify(webhookRepository).deleteById(webhookId);
    }

    @Test
    void test_delete_whenWebhookNotFound_throwNotFoundException() {
        Long webhookId = 100L;
        when(webhookRepository.existsById(webhookId)).thenReturn(false);

        NotFoundException exception = assertThrows(NotFoundException.class, () -> webhookService.delete(webhookId));
        assertNotNull(exception);
        verify(webhookEventRepository, never()).deleteByWebhookId(webhookId);
        verify(webhookRepository, never()).deleteById(webhookId);
    }
}
