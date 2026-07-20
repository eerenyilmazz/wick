package com.wick.session.service;

import com.wick.testsupport.TestEntities;

import com.wick.common.exception.ApiException;
import com.wick.common.exception.ContactInfoNotAllowedException;
import com.wick.match.domain.Match;
import com.wick.match.service.MatchService;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.MessageRepository;
import com.wick.session.service.impl.SessionMessageServiceImpl;
import com.wick.user.domain.User;
import com.wick.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionMessageServiceImplTest {

    @Mock
    private SessionService sessionService;

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private MatchService matchService;

    @Mock
    private UserService userService;

    @Mock
    private ContactInfoDetector contactInfoDetector;

    private SessionMessageServiceImpl sessionMessageService;

    private User userA;
    private Match match;

    @BeforeEach
    void setUp() {
        sessionMessageService = new SessionMessageServiceImpl(sessionService, messageRepository, matchService,
                userService, contactInfoDetector);
        userA = TestEntities.user("a@example.com", "hash", "A");
        User userB = TestEntities.user("b@example.com", "hash", "B");
        ReflectionTestUtils.setField(userA, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(userB, "id", UUID.randomUUID());
        match = TestEntities.match(userA, userB);
        ReflectionTestUtils.setField(match, "id", UUID.randomUUID());
    }

    @Test
    void sendMessage_throwsConflict_whenSessionIsLocked() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        session.setStatus(SessionStatus.LOCKED);
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionService.getById(session.getId())).thenReturn(session);

        assertThatThrownBy(() -> sessionMessageService.sendMessage(session.getId(), userA.getId(), "hi"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void sendMessage_throwsForbidden_whenSenderNotInMatch() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionService.getById(session.getId())).thenReturn(session);
        when(matchService.involves(match, userA.getId())).thenReturn(false);

        assertThatThrownBy(() -> sessionMessageService.sendMessage(session.getId(), userA.getId(), "hi"))
                .isInstanceOf(ApiException.class);
    }

    @Test
    void sendMessage_throwsContactInfoNotAllowed_whenContentContainsPhoneNumber() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionService.getById(session.getId())).thenReturn(session);
        when(matchService.involves(match, userA.getId())).thenReturn(true);
        when(contactInfoDetector.containsContactInfo("call me 0532 123 45 67")).thenReturn(true);

        assertThatThrownBy(() -> sessionMessageService.sendMessage(session.getId(), userA.getId(), "call me 0532 123 45 67"))
                .isInstanceOf(ContactInfoNotAllowedException.class);
    }

    @Test
    void getMessages_throwsForbidden_whenRequesterNotInMatch() {
        Session session = TestEntities.session(match, Instant.now(), Instant.now());
        ReflectionTestUtils.setField(session, "id", UUID.randomUUID());
        when(sessionService.getById(session.getId())).thenReturn(session);
        when(matchService.involves(match, userA.getId())).thenReturn(false);

        assertThatThrownBy(() -> sessionMessageService.getMessages(session.getId(), userA.getId()))
                .isInstanceOf(ApiException.class);
    }
}
