package com.wick.invitation.controller;

import com.wick.invitation.dto.InvitationResponse;
import com.wick.invitation.dto.SendInvitationRequest;
import com.wick.invitation.mapper.InvitationMapper;
import com.wick.invitation.service.InvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final InvitationMapper invitationMapper;

    @PostMapping
    public InvitationResponse send(@AuthenticationPrincipal String userId,
                                    @Valid @RequestBody SendInvitationRequest request) {
        var invitation = invitationService.send(request.matchId(), UUID.fromString(userId), request.useToken());
        return invitationMapper.toResponse(invitation);
    }

    @PostMapping("/{id}/accept")
    public InvitationResponse accept(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return invitationMapper.toResponse(invitationService.accept(id, UUID.fromString(userId)));
    }

    @PostMapping("/{id}/decline")
    public InvitationResponse decline(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return invitationMapper.toResponse(invitationService.decline(id, UUID.fromString(userId)));
    }

    @DeleteMapping("/{id}")
    public InvitationResponse cancel(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return invitationMapper.toResponse(invitationService.cancel(id, UUID.fromString(userId)));
    }

    @GetMapping("/incoming")
    public List<InvitationResponse> getIncoming(@AuthenticationPrincipal String userId) {
        return invitationService.getIncoming(UUID.fromString(userId)).stream()
                .map(invitationMapper::toResponse)
                .toList();
    }
}
