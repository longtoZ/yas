package com.yas.payment.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.payment.service.PaymentProviderService;
import com.yas.payment.viewmodel.paymentprovider.CreatePaymentVm;
import com.yas.payment.viewmodel.paymentprovider.PaymentProviderVm;
import com.yas.payment.viewmodel.paymentprovider.UpdatePaymentVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@WebMvcTest(controllers = PaymentProviderController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentProviderControllerTest {

    @MockitoBean
    private PaymentProviderService paymentProviderService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void testCreate_whenRequestIsValid_thenReturnPaymentProviderVm() throws Exception {
        CreatePaymentVm request = new CreatePaymentVm();
        request.setId("Paypal");
        request.setName("Paypal");
        request.setConfigureUrl("url");

        PaymentProviderVm response = new PaymentProviderVm("Paypal", "Paypal", "Desc", 1, 10L, "icon-url");
        when(paymentProviderService.create(any(CreatePaymentVm.class))).thenReturn(response);

        mockMvc.perform(post("/backoffice/payment-providers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(MockMvcResultMatchers.content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testUpdate_whenRequestIsValid_thenReturnPaymentProviderVm() throws Exception {
        UpdatePaymentVm request = new UpdatePaymentVm();
        request.setId("Paypal");
        request.setName("Paypal");
        request.setConfigureUrl("url");

        PaymentProviderVm response = new PaymentProviderVm("Paypal", "Paypal", "Desc", 1, 10L, "icon-url");
        when(paymentProviderService.update(any(UpdatePaymentVm.class))).thenReturn(response);

        mockMvc.perform(put("/backoffice/payment-providers")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void testGetAll_whenRequestIsValid_thenReturnListPaymentProviderVm() throws Exception {
        PaymentProviderVm response = new PaymentProviderVm("Paypal", "Paypal", "Desc", 1, 10L, "icon-url");
        List<PaymentProviderVm> list = List.of(response);

        when(paymentProviderService.getEnabledPaymentProviders(any(Pageable.class))).thenReturn(list);

        mockMvc.perform(get("/storefront/payment-providers")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(MockMvcResultMatchers.content().json(objectWriter.writeValueAsString(list)));
    }
}
