package com.wick.config.service.impl;

import com.wick.config.SystemProperties;
import com.wick.config.dto.AppConfigResponse;
import com.wick.config.service.AppConfigService;
import com.wick.invitation.config.InvitationProperties;
import com.wick.match.config.MatchProperties;
import com.wick.photo.config.PhotoProperties;
import com.wick.session.config.SessionProperties;
import com.wick.token.config.TokenProperties;
import com.wick.user.config.UserProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppConfigServiceImpl implements AppConfigService {

    private final UserProperties userProperties;
    private final PhotoProperties photoProperties;
    private final MatchProperties matchProperties;
    private final InvitationProperties invitationProperties;
    private final SessionProperties sessionProperties;
    private final TokenProperties tokenProperties;
    private final SystemProperties systemProperties;

    @Override
    public AppConfigResponse getConfig() {
        return new AppConfigResponse(
                userProperties.getMinimumAge(),
                userProperties.getMaximumAge(),
                photoProperties.getMaxPhotosPerUser(),
                matchProperties.getDailySwipeLimit(),
                invitationProperties.getDailyLimit(),
                sessionProperties.getFreeDurationMinutes(),
                sessionProperties.getPremiumDurationMinutes(),
                sessionProperties.getExtensionMinutes(),
                tokenProperties.getSessionExtensionCost(),
                tokenProperties.getExtraInvitationCost(),
                tokenProperties.getPremiumMonthlyBonus(),
                systemProperties.isMaintenanceMode(),
                systemProperties.getMinVersion(),
                invitationProperties.getMaxRetryInvitations(),
                tokenProperties.getSuperLikeCost()
        );
    }
}
