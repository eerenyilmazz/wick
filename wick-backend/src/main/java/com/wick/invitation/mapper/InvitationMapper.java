package com.wick.invitation.mapper;

import com.wick.invitation.domain.Invitation;
import com.wick.invitation.dto.InvitationResponse;
import org.springframework.stereotype.Component;

@Component
public class InvitationMapper {

    public InvitationResponse toResponse(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getMatch().getId(),
                invitation.getSender().getId(),
                invitation.getStatus(),
                invitation.getCreatedAt()
        );
    }
}
