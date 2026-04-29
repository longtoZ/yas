package com.yas.backofficebff.viewmodel;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthenticatedUserTest {

    @Test
    void recordStoresProvidedUsername() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser("backoffice-user");

        assertEquals("backoffice-user", authenticatedUser.username());
    }
}
