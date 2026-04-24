package com.yas.storefrontbff.viewmodel;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ViewModelTest {

    @Test
    void authenticationViewModelsExposeTheirState() {
        AuthenticatedUserVm authenticatedUser = new AuthenticatedUserVm("member1");
        AuthenticationInfoVm authenticationInfo = new AuthenticationInfoVm(true, authenticatedUser);

        assertThat(authenticatedUser.username()).isEqualTo("member1");
        assertThat(authenticationInfo.isAuthenticated()).isTrue();
        assertThat(authenticationInfo.authenticatedUser()).isEqualTo(authenticatedUser);
    }

    @Test
    void cartViewModelsExposeTheirState() {
        CartDetailVm cartDetail = new CartDetailVm(1L, 2L, 3);
        CartGetDetailVm cartGetDetail = new CartGetDetailVm(10L, "customer-1", List.of(cartDetail));
        CartItemVm cartItem = CartItemVm.fromCartDetailVm(cartDetail);

        assertThat(cartDetail.id()).isEqualTo(1L);
        assertThat(cartDetail.productId()).isEqualTo(2L);
        assertThat(cartDetail.quantity()).isEqualTo(3);
        assertThat(cartGetDetail.id()).isEqualTo(10L);
        assertThat(cartGetDetail.customerId()).isEqualTo("customer-1");
        assertThat(cartGetDetail.cartDetails()).containsExactly(cartDetail);
        assertThat(cartItem.productId()).isEqualTo(2L);
        assertThat(cartItem.quantity()).isEqualTo(3);
    }

    @Test
    void guestAndTokenViewModelsExposeTheirState() {
        GuestUserVm guestUser = new GuestUserVm("guest-1", "guest@example.com", "secret");
        TokenResponseVm tokenResponse = new TokenResponseVm("access-token", "refresh-token");

        assertThat(guestUser.userId()).isEqualTo("guest-1");
        assertThat(guestUser.email()).isEqualTo("guest@example.com");
        assertThat(guestUser.password()).isEqualTo("secret");
        assertThat(tokenResponse.accessToken()).isEqualTo("access-token");
        assertThat(tokenResponse.refreshToken()).isEqualTo("refresh-token");
    }
}
