package com.wick.payment.mapper;

import com.wick.payment.domain.Purchase;
import com.wick.payment.dto.PurchaseResponse;
import com.wick.token.service.TokenService;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentMapper {

    private final TokenService tokenService;
    private final UserService userService;

    public PurchaseResponse toResponse(Purchase purchase) {
        UUID userId = purchase.getUser().getId();
        return new PurchaseResponse(
                purchase.getId(),
                purchase.getProductId(),
                tokenService.getBalance(userId),
                userService.getById(userId).isPremium()
        );
    }
}
