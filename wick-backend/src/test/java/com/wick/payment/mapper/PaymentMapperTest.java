package com.wick.payment.mapper;

import com.wick.testsupport.TestEntities;

import com.wick.payment.domain.Purchase;
import com.wick.payment.dto.PurchaseResponse;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentMapperTest {

    @Mock
    private TokenService tokenService;

    @Mock
    private UserService userService;

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper(tokenService, userService);
    }

    @Test
    void toResponse_combinesPurchaseTokenBalanceAndPremiumStatus() {
        User user = TestEntities.user("a@example.com", "hash", "A");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        user.setPremium(true);
        Purchase purchase = TestEntities.purchase(user, "premium_monthly", "tok-1");
        ReflectionTestUtils.setField(purchase, "id", UUID.randomUUID());

        when(tokenService.getBalance(user.getId())).thenReturn(15);
        when(userService.getById(user.getId())).thenReturn(user);

        PurchaseResponse response = paymentMapper.toResponse(purchase);

        assertThat(response.productId()).isEqualTo("premium_monthly");
        assertThat(response.tokenBalance()).isEqualTo(15);
        assertThat(response.premium()).isTrue();
    }
}
