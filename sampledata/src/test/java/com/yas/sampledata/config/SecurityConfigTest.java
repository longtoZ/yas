package com.yas.sampledata.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SecurityConfigTest {

    @Test
    void jwtAuthenticationConverterForKeycloak_mapsRolesToAuthorities() {
        SecurityConfig config = new SecurityConfig();
        var converter = config.jwtAuthenticationConverterForKeycloak();

        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("realm_access", Map.of("roles", List.of("ADMIN", "USER")))
                .subject("user-1")
                .build();

        JwtAuthenticationToken auth = (JwtAuthenticationToken) converter.convert(jwt);

        assertEquals("user-1", auth.getName());
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }
}
