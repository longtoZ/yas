package com.yas.payment.service.provider.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.yas.payment.model.CapturedPayment;
import com.yas.payment.model.InitiatedPayment;
import com.yas.payment.model.enumeration.PaymentMethod;
import com.yas.payment.model.enumeration.PaymentStatus;
import com.yas.payment.paypal.service.PaypalService;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCapturePaymentResponse;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentRequest;
import com.yas.payment.paypal.viewmodel.PaypalCreatePaymentResponse;
import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PaypalHandlerTest {

    @Mock
    private PaymentProviderService paymentProviderService;

    @Mock
    private PaypalService paypalService;

    @InjectMocks
    private PaypalHandler paypalHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetProviderId_ShouldReturnPaypal() {
        assertEquals("PAYPAL", paypalHandler.getProviderId());
    }

    @Test
    void testInitPayment_ShouldReturnInitiatedPayment() {
        InitPaymentRequestVm request = new InitPaymentRequestVm("checkout1", BigDecimal.TEN, "PAYPAL");
        
        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId("PAYPAL"))
            .thenReturn("{\"clientId\":\"abc\"}");

        PaypalCreatePaymentResponse response = PaypalCreatePaymentResponse.builder()
            .status("CREATED")
            .paymentId("pay1")
            .redirectUrl("http://url")
            .build();
        when(paypalService.createPayment(any(PaypalCreatePaymentRequest.class))).thenReturn(response);

        InitiatedPayment result = paypalHandler.initPayment(request);

        assertEquals("CREATED", result.getStatus());
        assertEquals("pay1", result.getPaymentId());
        assertEquals("http://url", result.getRedirectUrl());
    }

    @Test
    void testCapturePayment_ShouldReturnCapturedPayment() {
        CapturePaymentRequestVm request = new CapturePaymentRequestVm("token1", "payer1");

        when(paymentProviderService.getAdditionalSettingsByPaymentProviderId("PAYPAL"))
            .thenReturn("{\"clientId\":\"abc\"}");

        PaypalCapturePaymentResponse response = PaypalCapturePaymentResponse.builder()
            .checkoutId("checkout1")
            .amount(BigDecimal.TEN)
            .paymentFee(BigDecimal.ONE)
            .gatewayTransactionId("tx1")
            .paymentMethod("PAYPAL")
            .paymentStatus("COMPLETED")
            .failureMessage("none")
            .build();
        when(paypalService.capturePayment(any(PaypalCapturePaymentRequest.class))).thenReturn(response);

        CapturedPayment result = paypalHandler.capturePayment(request);

        assertEquals("checkout1", result.getCheckoutId());
        assertEquals(BigDecimal.TEN, result.getAmount());
        assertEquals(BigDecimal.ONE, result.getPaymentFee());
        assertEquals("tx1", result.getGatewayTransactionId());
        assertEquals(PaymentMethod.PAYPAL, result.getPaymentMethod());
        assertEquals(PaymentStatus.COMPLETED, result.getPaymentStatus());
        assertEquals("none", result.getFailureMessage());
    }
}
