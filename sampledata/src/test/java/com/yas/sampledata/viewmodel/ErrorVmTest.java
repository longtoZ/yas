package com.yas.sampledata.viewmodel;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void constructor_defaultsFieldErrors() {
        ErrorVm vm = new ErrorVm("400", "Bad", "detail");

        assertNotNull(vm.fieldErrors());
        assertTrue(vm.fieldErrors().isEmpty());
    }
}
