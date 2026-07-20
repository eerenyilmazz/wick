package com.wick.session.service.impl;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ContactInfoNotAllowedException;
import com.wick.match.service.MatchService;
import com.wick.session.domain.Message;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.MessageRepository;
import com.wick.session.service.ContactInfoDetector;
import com.wick.session.service.SessionMessageService;
import com.wick.session.service.SessionService;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionMessageServiceImpl implements SessionMessageService {

    private final SessionService sessionService;
    private final MessageRepository messageRepository;
    private final MatchService matchService;
    private final UserService userService;
    private final ContactInfoDetector contactInfoDetector;

    @Override
    @Transactional
    public Message sendMessage(UUID sessionId, UUID senderId, String content, String clientMessageId) {
        if (clientMessageId != null) {
            var existing = messageRepository.findByClientMessageId(clientMessageId);
            if (existing.isPresent()) return existing.get();
        }
        Session session = sessionService.getById(sessionId);
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ApiException("Session is locked", HttpStatus.CONFLICT);
        }
        if (!matchService.involves(session.getMatch(), senderId)) {
            throw new ApiException("You are not part of this session", HttpStatus.FORBIDDEN);
        }
        if (contactInfoDetector.containsContactInfo(content)) {
            throw new ContactInfoNotAllowedException("Sharing contact info in chat is not allowed");
        }
        User sender = userService.getById(senderId);
        Message message = new Message();
        message.setSession(session);
        message.setSender(sender);
        message.setContent(content);
        message.setClientMessageId(clientMessageId);
        return messageRepository.save(message);
    }

    @Override
    public List<Message> getMessages(UUID sessionId, UUID requesterId, Instant since) {
        Session session = sessionService.getById(sessionId);
        if (!matchService.involves(session.getMatch(), requesterId)) {
            throw new ApiException("You are not part of this session", HttpStatus.FORBIDDEN);
        }
        if (since != null) {
            return messageRepository.findAllBySessionIdAndCreatedAtAfterOrderByCreatedAtAsc(sessionId, since);
        }
        return messageRepository.findAllBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
