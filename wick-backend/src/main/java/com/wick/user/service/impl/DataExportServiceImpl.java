package com.wick.user.service.impl;

import com.wick.invitation.mapper.InvitationMapper;
import com.wick.invitation.service.InvitationService;
import com.wick.match.mapper.MatchMapper;
import com.wick.match.service.MatchService;
import com.wick.payment.domain.Purchase;
import com.wick.payment.dto.PurchaseExportItem;
import com.wick.payment.service.PaymentService;
import com.wick.photo.mapper.PhotoMapper;
import com.wick.photo.service.UserPhotoService;
import com.wick.token.service.TokenService;
import com.wick.user.dto.DataExportResponse;
import com.wick.user.mapper.UserMapper;
import com.wick.user.service.DataExportService;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DataExportServiceImpl implements DataExportService {

    private final UserService userService;
    private final UserMapper userMapper;
    private final UserPhotoService userPhotoService;
    private final PhotoMapper photoMapper;
    private final MatchService matchService;
    private final MatchMapper matchMapper;
    private final InvitationService invitationService;
    private final InvitationMapper invitationMapper;
    private final PaymentService paymentService;
    private final TokenService tokenService;

    @Override
    public DataExportResponse export(UUID userId) {
        return new DataExportResponse(
                userMapper.toResponse(userService.getById(userId)),
                userPhotoService.getForUser(userId).stream().map(photoMapper::toResponse).toList(),
                matchService.getAllMatchesForUser(userId).stream().map(match -> matchMapper.toResponse(match, userId)).toList(),
                invitationService.getSentByUser(userId).stream().map(invitationMapper::toResponse).toList(),
                paymentService.getPurchasesForUser(userId).stream().map(this::toPurchaseExportItem).toList(),
                tokenService.getBalance(userId)
        );
    }

    private PurchaseExportItem toPurchaseExportItem(Purchase purchase) {
        return new PurchaseExportItem(purchase.getId(), purchase.getProductId(), purchase.getCreatedAt());
    }
}
