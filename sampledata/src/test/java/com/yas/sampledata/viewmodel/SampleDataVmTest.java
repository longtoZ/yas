package com.yas.sampledata.viewmodel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class SampleDataVmTest {

    @Test
    void recordStoresMessage() {
        SampleDataVm vm = new SampleDataVm("message");

        assertEquals("message", vm.message());
    }
}
