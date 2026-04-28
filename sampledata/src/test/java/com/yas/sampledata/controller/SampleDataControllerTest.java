package com.yas.sampledata.controller;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.yas.sampledata.service.SampleDataService;
import com.yas.sampledata.viewmodel.SampleDataVm;
import org.junit.jupiter.api.Test;

class SampleDataControllerTest {

    @Test
    void createSampleData_delegatesToService() {
        SampleDataService service = mock(SampleDataService.class);
        SampleDataVm expected = new SampleDataVm("ok");
        when(service.createSampleData()).thenReturn(expected);
        SampleDataController controller = new SampleDataController(service);

        SampleDataVm result = controller.createSampleData(new SampleDataVm("ignored"));

        assertSame(expected, result);
        verify(service).createSampleData();
    }
}
