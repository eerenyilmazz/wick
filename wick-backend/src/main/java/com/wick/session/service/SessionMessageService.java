package com.wick.session.service;

import com.wick.session.domain.Message;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SessionMessageService {

    Message sendMessage(UUID sessionId, UUID senderId, String content, String clientMessageId);

    List<Message> getMessages(UUID sessionId, UUID requesterId, Instant since);
}
