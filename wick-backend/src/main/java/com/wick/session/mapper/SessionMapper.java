package com.wick.session.mapper;

import com.wick.common.exception.ResourceNotFoundException;
import com.wick.match.service.MatchService;
import com.wick.session.domain.Message;
import com.wick.session.domain.Session;
import com.wick.session.dto.MessageResponse;
import com.wick.session.dto.SessionResponse;
import com.wick.session.dto.SessionSummaryResponse;
import com.wick.session.repository.MessageRepository;
import com.wick.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionMapper {

    private final MatchService matchService;
    private final UserMapper userMapper;
    private final MessageRepository messageRepository;

    public SessionResponse toResponse(Session session) {
        return new SessionResponse(
                session.getId(),
                session.getMatch().getId(),
                session.getStartedAt(),
                session.getEndsAt(),
                session.getStatus()
        );
    }

    public SessionResponse toResponseOrThrow(Optional<Session> session) {
        return session.map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No active session for this match"));
    }

    public SessionSummaryResponse toSummaryResponse(Session session, UUID viewerId) {
        String lastMessage = messageRepository.findTopBySessionIdOrderByCreatedAtDesc(session.getId())
                .map(Message::getContent)
                .orElse(null);
        return new SessionSummaryResponse(
                session.getId(),
                session.getMatch().getId(),
                session.getEndsAt(),
                userMapper.toResponse(matchService.otherUser(session.getMatch(), viewerId)),
                lastMessage,
                session.getStatus().name()
        );
    }

    public MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.getId(),
                message.getSession().getId(),
                message.getSender().getId(),
                message.getContent(),
                message.getCreatedAt(),
                message.getClientMessageId()
        );
    }
}
