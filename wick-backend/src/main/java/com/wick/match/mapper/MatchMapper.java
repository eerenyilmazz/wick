package com.wick.match.mapper;

import com.wick.invitation.mapper.InvitationMapper;
import com.wick.invitation.repository.InvitationRepository;
import com.wick.match.domain.Match;
import com.wick.match.dto.MatchResponse;
import com.wick.match.dto.SwipeResponse;
import com.wick.match.service.MatchService;
import com.wick.session.repository.SessionRepository;
import com.wick.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MatchMapper {

    private final UserMapper userMapper;
    private final MatchService matchService;
    private final InvitationRepository invitationRepository;
    private final InvitationMapper invitationMapper;
    private final SessionRepository sessionRepository;

    public MatchResponse toResponse(Match match, UUID viewerId) {
        var latestSession = sessionRepository.findTopByMatchIdOrderByStartedAtDesc(match.getId());
        String sessionStatus = latestSession.map(s -> s.getStatus().name()).orElse(null);
        java.time.Instant sessionEndsAt = latestSession.map(s -> s.getEndsAt()).orElse(null);
        return new MatchResponse(
                match.getId(),
                userMapper.toResponse(matchService.otherUser(match, viewerId)),
                match.getCreatedAt(),
                invitationRepository.findTopByMatchIdOrderByCreatedAtDesc(match.getId())
                        .map(invitationMapper::toResponse)
                        .orElse(null),
                sessionStatus,
                sessionEndsAt
        );
    }

    public SwipeResponse toSwipeResponse(Optional<Match> match) {
        return match.map(m -> new SwipeResponse(true, m.getId()))
                .orElseGet(() -> new SwipeResponse(false, null));
    }
}
