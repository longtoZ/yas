package com.yas.tax.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.yas.tax.model.TaxClass;
import com.yas.tax.model.TaxRate;
import com.yas.tax.service.TaxRateService;
import com.yas.tax.viewmodel.taxrate.TaxRateListGetVm;
import com.yas.tax.viewmodel.taxrate.TaxRatePostVm;
import com.yas.tax.viewmodel.taxrate.TaxRateVm;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectWriter;

@WebMvcTest(controllers = TaxRateController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxRateControllerTest {

    @MockitoBean
    private TaxRateService taxRateService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void getPageableTaxRates_WhenNormalCase_ReturnPageVm() throws Exception {
        TaxRateListGetVm response = new TaxRateListGetVm(List.of(), 0, 10, 0, 0, true);
        when(taxRateService.getPageableTaxRates(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/backoffice/tax-rates/paging")
                .param("pageNo", "0")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void getTaxRate_WhenNormalCase_ReturnTaxRateVm() throws Exception {
        TaxRateVm response = new TaxRateVm(1L, 0.1, "70000", 2L, 10L, 84L);
        when(taxRateService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/backoffice/tax-rates/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void createTaxRate_WhenNormalCase_ReturnCreatedTaxRate() throws Exception {
        TaxClass taxClass = TaxClass.builder().id(2L).name("Standard").build();
        TaxRate taxRate = TaxRate.builder()
            .id(7L)
            .rate(0.1)
            .zipCode("70000")
            .taxClass(taxClass)
            .stateOrProvinceId(10L)
            .countryId(84L)
            .build();
        TaxRatePostVm request = new TaxRatePostVm(0.1, "70000", 2L, 10L, 84L);
        when(taxRateService.createTaxRate(any(TaxRatePostVm.class))).thenReturn(taxRate);

        mockMvc.perform(post("/backoffice/tax-rates")
                .contentType("application/json")
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/tax-rates/7"))
            .andExpect(content().json(objectWriter.writeValueAsString(TaxRateVm.fromModel(taxRate))));
    }

    @Test
    void updateTaxRate_WhenNormalCase_ReturnNoContent() throws Exception {
        TaxRatePostVm request = new TaxRatePostVm(0.15, "70000", 2L, 10L, 84L);
        doNothing().when(taxRateService).updateTaxRate(any(TaxRatePostVm.class), anyLong());

        mockMvc.perform(put("/backoffice/tax-rates/{id}", 1L)
                .contentType("application/json")
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaxRate_WhenNormalCase_ReturnNoContent() throws Exception {
        doNothing().when(taxRateService).delete(1L);

        mockMvc.perform(delete("/backoffice/tax-rates/{id}", 1L))
            .andExpect(status().isNoContent());
    }

    @Test
    void getTaxPercentByAddress_WhenNormalCase_ReturnDoubleValue() throws Exception {
        when(taxRateService.getTaxPercent(2L, 84L, 10L, "70000")).thenReturn(0.1);

        mockMvc.perform(get("/backoffice/tax-rates/tax-percent")
                .param("taxClassId", "2")
                .param("countryId", "84")
                .param("stateOrProvinceId", "10")
                .param("zipCode", "70000"))
            .andExpect(status().isOk())
            .andExpect(content().string("0.1"));
    }

    @Test
    void getBatchTaxPercentsByAddress_WhenNormalCase_ReturnTaxRateList() throws Exception {
        List<TaxRateVm> response = List.of(new TaxRateVm(1L, 0.1, "70000", 2L, 10L, 84L));
        when(taxRateService.getBulkTaxRate(List.of(2L, 3L), 84L, 10L, "70000")).thenReturn(response);

        mockMvc.perform(get("/backoffice/tax-rates/location-based-batch")
                .param("taxClassIds", "2", "3")
                .param("countryId", "84")
                .param("stateOrProvinceId", "10")
                .param("zipCode", "70000"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }
}
