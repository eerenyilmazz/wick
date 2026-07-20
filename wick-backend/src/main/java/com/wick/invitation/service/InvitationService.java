package com.wick.invitation.service;

import com.wick.invitation.domain.Invitation;

import java.util.List;
import java.util.UUID;

public interface InvitationService {

    Invitation send(UUID matchId, UUID senderId, boolean useToken);

    Invitation accept(UUID invitationId, UUID userId);

    Invitation decline(UUID invitationId, UUID userId);

    List<Invitation> getIncoming(UUID userId);

    Invitation cancel(UUID invitationId, UUID userId);

    List<Invitation> getSentByUser(UUID userId);
}
