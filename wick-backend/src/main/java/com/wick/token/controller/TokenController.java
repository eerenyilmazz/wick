package com.wick.token.controller;

import com.wick.common.dto.PageResponse;
import com.wick.token.dto.TokenBalanceResponse;
import com.wick.token.dto.TokenTransactionResponse;
import com.wick.token.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    @GetMapping("/balance")
    public TokenBalanceResponse getBalance(@AuthenticationPrincipal String userId) {
        return new TokenBalanceResponse(tokenService.getBalance(UUID.fromString(userId)));
    }

    @GetMapping("/daily-login/status")
    public Map<String, Boolean> getDailyLoginStatus(@AuthenticationPrincipal String userId) {
        return Map.of("claimed", tokenService.isDailyLoginClaimedToday(UUID.fromString(userId)));
    }

    @PostMapping("/daily-login")
    public TokenBalanceResponse claimDailyLogin(@AuthenticationPrincipal String userId) {
        return new TokenBalanceResponse(tokenService.claimDailyLogin(UUID.fromString(userId)));
    }

    @GetMapping("/transactions")
    public PageResponse<TokenTransactionResponse> getTransactions(@AuthenticationPrincipal String userId,
                                                                   @PageableDefault(size = 20) Pageable pageable) {
        return tokenService.getTransactions(UUID.fromString(userId), pageable);
    }
}
