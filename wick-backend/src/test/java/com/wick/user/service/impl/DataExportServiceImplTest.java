package com.wick.user.service.impl;

import com.wick.testsupport.TestEntities;

import com.wick.invitation.domain.Invitation;
import com.wick.invitation.mapper.InvitationMapper;
import com.wick.invitation.service.InvitationService;
import com.wick.match.domain.Match;
import com.wick.match.mapper.MatchMapper;
import com.wick.match.service.MatchService;
import com.wick.payment.domain.Purchase;
import com.wick.payment.service.PaymentService;
import com.wick.photo.domain.UserPhoto;
import com.wick.photo.mapper.PhotoMapper;
import com.wick.photo.service.UserPhotoService;
import com.wick.token.service.TokenService;
import com.wick.user.domain.User;
import com.wick.user.dto.DataExportResponse;
import com.wick.user.mapper.UserMapper;
import com.wick.user.dto.UserResponse;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataExportServiceImplTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private UserPhotoService userPhotoService;

    @Mock
    private PhotoMapper photoMapper;

    @Mock
    private MatchService matchService;

    @Mock
    private MatchMapper matchMapper;

    @Mock
    private InvitationService invitationService;

    @Mock
    private InvitationMapper invitationMapper;

    @Mock
    private PaymentService paymentService;

    @Mock
    private TokenService tokenService;

    private DataExportServiceImpl dataExportService;

    @BeforeEach
    void setUp() {
        dataExportService = new DataExportServiceImpl(userService, userMapper, userPhotoService, photoMapper,
                matchService, matchMapper, invitationService, invitationMapper, paymentService, tokenService);
    }

    @Test
    void export_aggregatesAllUserDataAcrossServices() {
        UUID userId = UUID.randomUUID();
        User user = TestEntities.user("eren@example.com", "hash", "Eren");
        ReflectionTestUtils.setField(user, "id", userId);
        UserResponse userResponse = new UserResponse(userId, "eren@example.com", "Eren", null, null, null, List.of(), null, null, List.of(), false, false, null, false);
        when(userService.getById(userId)).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserPhoto photo = TestEntities.userPhoto(user, "key.jpg", 0);
        when(userPhotoService.getForUser(userId)).thenReturn(List.of(photo));

        Match match = TestEntities.match(user, user);
        when(matchService.getAllMatchesForUser(userId)).thenReturn(List.of(match));

        Invitation invitation = TestEntities.invitation(match, user);
        when(invitationService.getSentByUser(userId)).thenReturn(List.of(invitation));

        Purchase purchase = TestEntities.purchase(user, "token_pack_20", "purchase-token-1");
        when(paymentService.getPurchasesForUser(userId)).thenReturn(List.of(purchase));

        when(tokenService.getBalance(userId)).thenReturn(7);

        DataExportResponse result = dataExportService.export(userId);

        assertThat(result.profile()).isEqualTo(userResponse);
        assertThat(result.photos()).hasSize(1);
        assertThat(result.matches()).hasSize(1);
        assertThat(result.sentInvitations()).hasSize(1);
        assertThat(result.purchases()).hasSize(1);
        assertThat(result.purchases().get(0).productId()).isEqualTo("token_pack_20");
        assertThat(result.tokenBalance()).isEqualTo(7);
    }
}
