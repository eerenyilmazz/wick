package com.wick.payment.service;

import com.wick.testsupport.TestEntities;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wick.common.exception.ApiException;
import com.wick.common.exception.ConflictException;
import com.wick.common.exception.ResourceNotFoundException;
import com.wick.eventlog.service.EventLogService;
import com.wick.payment.config.BillingProperties;
import com.wick.payment.domain.Purchase;
import com.wick.payment.domain.ProductType;
import com.wick.payment.repository.PurchaseRepository;
import com.wick.payment.service.impl.PaymentServiceImpl;
import com.wick.token.config.TokenProperties;
import com.wick.token.domain.TokenReason;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private PurchaseRepository purchaseRepository;

    @Mock
    private PlayBillingVerifier playBillingVerifier;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private EventLogService eventLogService;

    private BillingProperties billingProperties;
    private PaymentServiceImpl paymentService;

    private User user;

    @BeforeEach
    void setUp() {
        billingProperties = new BillingProperties();
        TokenProperties tokenProperties = new TokenProperties();
        tokenProperties.setPremiumMonthlyBonus(10);

        paymentService = new PaymentServiceImpl(purchaseRepository, playBillingVerifier, billingProperties,
                userService, tokenService, tokenProperties, new ObjectMapper(), eventLogService);

        user = TestEntities.user("a@example.com", "hash", "A");
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
    }

    private String rtdnPayload(int notificationType, String purchaseToken) {
        String json = """
                {"packageName":"com.wickapp.wick","subscriptionNotification":{"notificationType":%d,"purchaseToken":"%s","subscriptionId":"premium_monthly"}}
                """.formatted(notificationType, purchaseToken);
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    private void registerProduct(String productId, ProductType type, int amount) {
        BillingProperties.ProductConfig config = new BillingProperties.ProductConfig();
        config.setType(type);
        config.setAmount(amount);
        billingProperties.setProducts(Map.of(productId, config));
    }

    @Test
    void processPurchase_throwsConflict_whenTokenAlreadyProcessed() {
        when(purchaseRepository.existsByPurchaseToken("tok-1")).thenReturn(true);

        assertThatThrownBy(() -> paymentService.processPurchase(user.getId(), "token_pack_5", "tok-1"))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void processPurchase_throwsBadRequest_whenVerifierRejects() {
        when(purchaseRepository.existsByPurchaseToken("tok-1")).thenReturn(false);
        when(playBillingVerifier.isValid("token_pack_5", "tok-1")).thenReturn(false);

        assertThatThrownBy(() -> paymentService.processPurchase(user.getId(), "token_pack_5", "tok-1"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void processPurchase_throwsNotFound_whenProductUnknown() {
        when(purchaseRepository.existsByPurchaseToken("tok-1")).thenReturn(false);
        when(playBillingVerifier.isValid(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> paymentService.processPurchase(user.getId(), "unknown_product", "tok-1"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processPurchase_grantsTokens_forTokenPackProduct() {
        registerProduct("token_pack_5", ProductType.TOKEN_PACK, 5);
        when(purchaseRepository.existsByPurchaseToken("tok-1")).thenReturn(false);
        when(playBillingVerifier.isValid("token_pack_5", "tok-1")).thenReturn(true);
        when(userService.getById(user.getId())).thenReturn(user);
        when(purchaseRepository.saveAndFlush(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        Purchase result = paymentService.processPurchase(user.getId(), "token_pack_5", "tok-1");

        verify(tokenService).earn(user.getId(), 5, TokenReason.PURCHASED);
        verify(userService, never()).grantPremium(any());
        assertThat(result.getProductId()).isEqualTo("token_pack_5");
    }

    @Test
    void processPurchase_doesNotGrantEntitlement_whenSaveLosesConcurrentRace() {
        registerProduct("token_pack_5", ProductType.TOKEN_PACK, 5);
        when(purchaseRepository.existsByPurchaseToken("tok-3")).thenReturn(false);
        when(playBillingVerifier.isValid("token_pack_5", "tok-3")).thenReturn(true);
        when(userService.getById(user.getId())).thenReturn(user);
        when(purchaseRepository.saveAndFlush(any(Purchase.class)))
                .thenThrow(new org.springframework.dao.DataIntegrityViolationException("duplicate token"));

        assertThatThrownBy(() -> paymentService.processPurchase(user.getId(), "token_pack_5", "tok-3"))
                .isInstanceOf(ConflictException.class);

        verify(tokenService, never()).earn(any(), anyInt(), any());
        verify(userService, never()).grantPremium(any());
    }

    @Test
    void processPurchase_grantsPremiumAndBonusTokens_forSubscriptionProduct() {
        registerProduct("premium_monthly", ProductType.PREMIUM_SUBSCRIPTION, 1);
        when(purchaseRepository.existsByPurchaseToken("tok-2")).thenReturn(false);
        when(playBillingVerifier.isValid("premium_monthly", "tok-2")).thenReturn(true);
        when(userService.getById(user.getId())).thenReturn(user);
        when(purchaseRepository.saveAndFlush(any(Purchase.class))).thenAnswer(inv -> inv.getArgument(0));

        paymentService.processPurchase(user.getId(), "premium_monthly", "tok-2");

        verify(userService).grantPremium(user.getId());
        verify(tokenService).earn(user.getId(), 10, TokenReason.PREMIUM_GRANT);
    }

    @Test
    void handlePushNotification_grantsPremium_whenSubscriptionRenewed() {
        Purchase purchase = TestEntities.purchase(user, "premium_monthly", "rtdn-tok-1");
        when(purchaseRepository.findByPurchaseToken("rtdn-tok-1")).thenReturn(Optional.of(purchase));

        paymentService.handlePushNotification(rtdnPayload(2, "rtdn-tok-1"));

        verify(userService).grantPremium(user.getId());
        verify(userService, never()).revokePremium(any());
    }

    @Test
    void handlePushNotification_revokesPremium_whenSubscriptionExpired() {
        Purchase purchase = TestEntities.purchase(user, "premium_monthly", "rtdn-tok-2");
        when(purchaseRepository.findByPurchaseToken("rtdn-tok-2")).thenReturn(Optional.of(purchase));

        paymentService.handlePushNotification(rtdnPayload(13, "rtdn-tok-2"));

        verify(userService).revokePremium(user.getId());
        verify(userService, never()).grantPremium(any());
    }

    @Test
    void handlePushNotification_doesNothing_whenNotificationTypeRequiresNoAction() {
        paymentService.handlePushNotification(rtdnPayload(3, "rtdn-tok-3"));

        verify(purchaseRepository, never()).findByPurchaseToken(any());
        verify(userService, never()).grantPremium(any());
        verify(userService, never()).revokePremium(any());
    }

    @Test
    void handlePushNotification_throwsNotFound_whenPurchaseTokenIsUnknown() {
        when(purchaseRepository.findByPurchaseToken("missing-tok")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.handlePushNotification(rtdnPayload(2, "missing-tok")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
