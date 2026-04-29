package com.yas.tax.viewmodel.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void compactConstructor_WhenFieldErrorsProvided_StoreAllValues() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "Invalid tax rate", List.of("rate"));

        assertThat(errorVm.statusCode()).isEqualTo("400");
        assertThat(errorVm.title()).isEqualTo("Bad Request");
        assertThat(errorVm.detail()).isEqualTo("Invalid tax rate");
        assertThat(errorVm.fieldErrors()).containsExactly("rate");
    }

    @Test
    void overloadedConstructor_WhenFieldErrorsOmitted_CreateEmptyList() {
        ErrorVm errorVm = new ErrorVm("404", "Not Found", "Tax rate not found");

        assertThat(errorVm.statusCode()).isEqualTo("404");
        assertThat(errorVm.title()).isEqualTo("Not Found");
        assertThat(errorVm.detail()).isEqualTo("Tax rate not found");
        assertThat(errorVm.fieldErrors()).isEmpty();
    }
}
