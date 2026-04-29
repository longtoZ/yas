package com.yas.tax.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ConstantsTest {

    @Test
    void constructor_WhenInstantiated_ReturnObject() {
        Constants constants = new Constants();

        assertThat(constants).isNotNull();
    }
}
