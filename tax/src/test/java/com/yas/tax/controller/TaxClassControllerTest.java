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
import com.yas.tax.service.TaxClassService;
import com.yas.tax.viewmodel.taxclass.TaxClassListGetVm;
import com.yas.tax.viewmodel.taxclass.TaxClassPostVm;
import com.yas.tax.viewmodel.taxclass.TaxClassVm;
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

@WebMvcTest(controllers = TaxClassController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class TaxClassControllerTest {

    @MockitoBean
    private TaxClassService taxClassService;

    @Autowired
    private MockMvc mockMvc;

    private ObjectWriter objectWriter;

    @BeforeEach
    void setUp() {
        objectWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
    }

    @Test
    void getPageableTaxClasses_WhenNormalCase_ReturnPageVm() throws Exception {
        TaxClassListGetVm response = new TaxClassListGetVm(List.of(new TaxClassVm(1L, "Standard")), 0, 10, 1, 1, true);
        when(taxClassService.getPageableTaxClasses(anyInt(), anyInt())).thenReturn(response);

        mockMvc.perform(get("/backoffice/tax-classes/paging")
                .param("pageNo", "0")
                .param("pageSize", "10"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void listTaxClasses_WhenNormalCase_ReturnTaxClassList() throws Exception {
        List<TaxClassVm> response = List.of(new TaxClassVm(1L, "Standard"));
        when(taxClassService.findAllTaxClasses()).thenReturn(response);

        mockMvc.perform(get("/backoffice/tax-classes"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void getTaxClass_WhenNormalCase_ReturnTaxClassVm() throws Exception {
        TaxClassVm response = new TaxClassVm(1L, "Standard");
        when(taxClassService.findById(1L)).thenReturn(response);

        mockMvc.perform(get("/backoffice/tax-classes/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().json(objectWriter.writeValueAsString(response)));
    }

    @Test
    void createTaxClass_WhenNormalCase_ReturnCreatedTaxClass() throws Exception {
        TaxClass taxClass = TaxClass.builder().id(7L).name("Standard").build();
        TaxClassPostVm request = new TaxClassPostVm("1", "Standard");
        when(taxClassService.create(any(TaxClassPostVm.class))).thenReturn(taxClass);

        mockMvc.perform(post("/backoffice/tax-classes")
                .contentType("application/json")
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", "http://localhost/tax-classes/7"))
            .andExpect(content().json(objectWriter.writeValueAsString(TaxClassVm.fromModel(taxClass))));
    }

    @Test
    void updateTaxClass_WhenNormalCase_ReturnNoContent() throws Exception {
        TaxClassPostVm request = new TaxClassPostVm("1", "Updated");
        doNothing().when(taxClassService).update(any(TaxClassPostVm.class), anyLong());

        mockMvc.perform(put("/backoffice/tax-classes/{id}", 1L)
                .contentType("application/json")
                .content(objectWriter.writeValueAsString(request)))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteTaxClass_WhenNormalCase_ReturnNoContent() throws Exception {
        doNothing().when(taxClassService).delete(1L);

        mockMvc.perform(delete("/backoffice/tax-classes/{id}", 1L))
            .andExpect(status().isNoContent());
    }
}
