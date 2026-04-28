package com.yas.inventory.util;

import static com.yas.inventory.util.SecurityContextUtils.setUpSecurityContext;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.yas.commonlibrary.exception.AccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class AuthenticationUtilsTest {

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testExtractUserId_whenValidJwtToken_returnUserId() {
        String userId = "test-user-123";
        setUpJwtAuthentication(userId);

        String result = com.yas.inventory.utils.AuthenticationUtils.extractUserId();

        assertThat(result).isEqualTo(userId);
    }

    @Test
    void testExtractUserId_whenAnonymousToken_throwAccessDeniedException() {
        AnonymousAuthenticationToken anonymousToken = new AnonymousAuthenticationToken(
                "key", "anonymous", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonymousToken);

        assertThrows(AccessDeniedException.class,
                () -> com.yas.inventory.utils.AuthenticationUtils.extractUserId());
    }

    @Test
    void testExtractJwt_whenValidJwtToken_returnTokenValue() {
        String userId = "test-user-123";
        setUpSecurityContext(userId);

        String result = com.yas.inventory.utils.AuthenticationUtils.extractJwt();

        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    void testExtractJwt_whenMultipleCalls_returnConsistentToken() {
        String userId = "test-user-456";
        setUpSecurityContext(userId);

        String token1 = com.yas.inventory.utils.AuthenticationUtils.extractJwt();
        String token2 = com.yas.inventory.utils.AuthenticationUtils.extractJwt();

        assertThat(token1).isEqualTo(token2);
    }

    @Test
    void testExtractJwt_whenCombinedWithExtractUserId_bothReturnValidValues() {
        String userId = "combined-test-user";
        setUpJwtAuthentication(userId);

        String extractedUserId = com.yas.inventory.utils.AuthenticationUtils.extractUserId();
        String extractedJwt = com.yas.inventory.utils.AuthenticationUtils.extractJwt();

        assertThat(extractedUserId).isEqualTo(userId);
        assertThat(extractedJwt).isNotNull();
        assertThat(extractedJwt).isNotEmpty();
    }

    private void setUpJwtAuthentication(String userId) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(userId)
                .build();
        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}
