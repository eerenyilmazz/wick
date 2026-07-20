package com.wick.session.controller;

import com.wick.common.exception.ApiException;
import com.wick.session.dto.MessageResponse;
import com.wick.session.dto.SendMessageRequest;
import com.wick.session.mapper.SessionMapper;
import com.wick.session.service.SessionMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

import java.security.Principal;
import java.util.UUID;

@Controller
@Validated
@RequiredArgsConstructor
public class SessionMessageController {

    private final SessionMessageService sessionMessageService;
    private final SessionMapper sessionMapper;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/sessions/{sessionId}/typing")
    public void typing(@DestinationVariable UUID sessionId, Principal principal) {
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId + "/typing", principal.getName());
    }

    @MessageMapping("/sessions/{sessionId}/messages")
    public void sendMessage(@DestinationVariable UUID sessionId,
                             @Valid @Payload SendMessageRequest request,
                             Principal principal) {
        MessageResponse response = sessionMapper.toResponse(
                sessionMessageService.sendMessage(sessionId, UUID.fromString(principal.getName()), request.content(), request.clientMessageId()));
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId, response);
    }

    @MessageExceptionHandler
    public void handleError(ApiException ex, Principal principal) {
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", ex.getMessage());
        }
    }

    @MessageExceptionHandler
    public void handleValidationError(MethodArgumentNotValidException ex, Principal principal) {
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", "Invalid message");
        }
    }

    @MessageExceptionHandler(Exception.class)
    public void handleUnexpectedError(Exception ex, Principal principal) {
        if (principal != null) {
            messagingTemplate.convertAndSendToUser(principal.getName(), "/queue/errors", "An unexpected error occurred");
        }
    }
}
