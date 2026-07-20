package com.wick.session.controller;

import com.wick.common.exception.ApiException;
import com.wick.session.domain.Session;
import com.wick.session.dto.MessageResponse;
import com.wick.session.dto.SessionResponse;
import com.wick.session.dto.SessionSummaryResponse;
import com.wick.session.mapper.SessionMapper;
import com.wick.session.service.SessionMessageService;
import com.wick.session.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final SessionMessageService sessionMessageService;
    private final SessionMapper sessionMapper;

    @GetMapping("/my")
    public List<SessionSummaryResponse> getMySessions(@AuthenticationPrincipal String userId) {
        return sessionService.findRecentSessions(UUID.fromString(userId)).stream()
                .map(s -> sessionMapper.toSummaryResponse(s, UUID.fromString(userId)))
                .toList();
    }

    @GetMapping("/active")
    public SessionResponse getActiveForMatch(@AuthenticationPrincipal String userId, @RequestParam UUID matchId) {
        java.util.Optional<Session> sessionOpt = sessionService.findActiveByMatch(matchId);
        sessionOpt.ifPresent(session -> {
            String userOneId = session.getMatch().getUserOne().getId().toString();
            String userTwoId = session.getMatch().getUserTwo().getId().toString();
            if (!userId.equals(userOneId) && !userId.equals(userTwoId)) {
                throw new ApiException("Forbidden", HttpStatus.FORBIDDEN);
            }
        });
        return sessionMapper.toResponseOrThrow(sessionOpt);
    }

    @GetMapping("/for-match")
    public SessionResponse getLatestForMatch(@AuthenticationPrincipal String userId, @RequestParam UUID matchId) {
        java.util.Optional<Session> sessionOpt = sessionService.findLatestByMatch(matchId);
        sessionOpt.ifPresent(session -> {
            String userOneId = session.getMatch().getUserOne().getId().toString();
            String userTwoId = session.getMatch().getUserTwo().getId().toString();
            if (!userId.equals(userOneId) && !userId.equals(userTwoId)) {
                throw new ApiException("Forbidden", HttpStatus.FORBIDDEN);
            }
        });
        return sessionMapper.toResponseOrThrow(sessionOpt);
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> getMessages(@AuthenticationPrincipal String userId,
                                              @PathVariable UUID id,
                                              @RequestParam(required = false) Instant since) {
        return sessionMessageService.getMessages(id, UUID.fromString(userId), since).stream()
                .map(sessionMapper::toResponse)
                .toList();
    }

    @PostMapping("/{id}/extend")
    public SessionResponse extend(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        return sessionMapper.toResponse(sessionService.extend(id, UUID.fromString(userId)));
    }

    @PostMapping("/{id}/end")
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.NO_CONTENT)
    public void end(@AuthenticationPrincipal String userId, @PathVariable UUID id) {
        sessionService.end(id, UUID.fromString(userId));
    }
}
