package com.yas.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.payment.service.PaymentService;
import com.yas.payment.viewmodel.CapturePaymentRequestVm;
import com.yas.payment.viewmodel.CapturePaymentResponseVm;
import com.yas.payment.viewmodel.InitPaymentRequestVm;
import com.yas.payment.viewmodel.InitPaymentResponseVm;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@WebMvcTest(controllers = PaymentController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @MockitoBean
    private PaymentService paymentService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void testInitPayment_whenRequestIsValid_thenReturnInitPaymentResponseVm() throws Exception {
        InitPaymentRequestVm request = new InitPaymentRequestVm(
            "checkoutId-123",
            new BigDecimal("100.00"),
            "paymentMethod-1"
        );

        InitPaymentResponseVm response = new InitPaymentResponseVm("url-to-redirect", "status", "id");
        when(paymentService.initPayment(any(InitPaymentRequestVm.class))).thenReturn(response);

        mockMvc.perform(post("/init")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testCapturePayment_whenRequestIsValid_thenReturnCapturePaymentResponseVm() throws Exception {
        CapturePaymentRequestVm request = new CapturePaymentRequestVm("token-123", "PayerId-123");
        CapturePaymentResponseVm response = new CapturePaymentResponseVm(1L, "checkout-123", BigDecimal.TEN, BigDecimal.ZERO, "abc", com.yas.payment.model.enumeration.PaymentMethod.BANKING, com.yas.payment.model.enumeration.PaymentStatus.COMPLETED, null);
        when(paymentService.capturePayment(any(CapturePaymentRequestVm.class))).thenReturn(response);

        mockMvc.perform(post("/capture")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testCancelPayment_thenReturnSuccessMessage() throws Exception {
        mockMvc.perform(get("/cancel")
                .accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().string("Payment cancelled"));
    }
}
