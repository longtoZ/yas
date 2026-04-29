package com.yas.inventory.viewmodel.error;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ErrorVmTest {

    @Test
    void testErrorVmConstructor_withAllParameters_createsValidObject() {
        String statusCode = "400";
        String title = "Bad Request";
        String detail = "Invalid input";
        List<String> fieldErrors = List.of("field1 is invalid", "field2 is required");

        ErrorVm errorVm = new ErrorVm(statusCode, title, detail, fieldErrors);

        assertThat(errorVm.statusCode()).isEqualTo("400");
        assertThat(errorVm.title()).isEqualTo("Bad Request");
        assertThat(errorVm.detail()).isEqualTo("Invalid input");
        assertThat(errorVm.fieldErrors()).hasSize(2);
        assertThat(errorVm.fieldErrors()).containsExactly("field1 is invalid", "field2 is required");
    }

    @Test
    void testErrorVmConstructor_withThreeParameters_initializesEmptyFieldErrors() {
        String statusCode = "404";
        String title = "Not Found";
        String detail = "Resource not found";

        ErrorVm errorVm = new ErrorVm(statusCode, title, detail);

        assertThat(errorVm.statusCode()).isEqualTo("404");
        assertThat(errorVm.title()).isEqualTo("Not Found");
        assertThat(errorVm.detail()).isEqualTo("Resource not found");
        assertThat(errorVm.fieldErrors()).isEmpty();
        assertThat(errorVm.fieldErrors()).isInstanceOf(ArrayList.class);
    }

    @Test
    void testErrorVmRecord_withMultipleFieldErrors_containsAllErrors() {
        List<String> fieldErrors = List.of("error1", "error2", "error3");
        ErrorVm errorVm = new ErrorVm("422", "Validation Error", "Multiple fields invalid", fieldErrors);

        assertThat(errorVm.fieldErrors()).hasSize(3);
        assertThat(errorVm.fieldErrors()).contains("error1", "error2", "error3");
    }

    @Test
    void testErrorVmRecord_statusCodeVariations_storedCorrectly() {
        ErrorVm badRequest = new ErrorVm("400", "Bad Request", "Invalid request");
        ErrorVm unauthorized = new ErrorVm("401", "Unauthorized", "Authentication required");
        ErrorVm notFound = new ErrorVm("404", "Not Found", "Resource not found");
        ErrorVm serverError = new ErrorVm("500", "Internal Server Error", "Server error occurred");

        assertThat(badRequest.statusCode()).isEqualTo("400");
        assertThat(unauthorized.statusCode()).isEqualTo("401");
        assertThat(notFound.statusCode()).isEqualTo("404");
        assertThat(serverError.statusCode()).isEqualTo("500");
    }

    @Test
    void testErrorVmRecord_nullFieldErrors_initializedAsEmpty() {
        ErrorVm errorVm = new ErrorVm("500", "Error", "An error occurred");

        assertThat(errorVm.fieldErrors()).isNotNull();
        assertThat(errorVm.fieldErrors()).isEmpty();
    }

    @Test
    void testErrorVmRecord_fieldErrorsModification_reflectsChanges() {
        List<String> fieldErrors = new ArrayList<>();
        fieldErrors.add("Field A is required");
        fieldErrors.add("Field B must be positive");

        ErrorVm errorVm = new ErrorVm("422", "Validation Failed", "Input validation error", fieldErrors);

        assertThat(errorVm.fieldErrors()).hasSize(2);
        assertThat(errorVm.fieldErrors()).contains("Field A is required", "Field B must be positive");
    }

    @Test
    void testErrorVmRecord_emptyDetail_acceptedAsValidValue() {
        ErrorVm errorVm = new ErrorVm("400", "Bad Request", "");

        assertThat(errorVm.detail()).isEmpty();
        assertThat(errorVm.title()).isEqualTo("Bad Request");
    }
}
