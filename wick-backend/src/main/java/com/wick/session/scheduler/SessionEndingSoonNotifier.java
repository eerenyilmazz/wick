package com.wick.session.scheduler;

import com.wick.match.domain.Match;
import com.wick.notification.config.NotificationProperties;
import com.wick.notification.service.NotificationService;
import com.wick.session.domain.Session;
import com.wick.session.domain.SessionStatus;
import com.wick.session.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SessionEndingSoonNotifier {

    private static final String WARNED_KEY_PREFIX = "session-ending-warned:";

    private final SessionRepository sessionRepository;
    private final NotificationService notificationService;
    private final NotificationProperties notificationProperties;
    private final StringRedisTemplate redisTemplate;

    @Scheduled(fixedDelayString = "${app.session.countdown-interval-ms}")
    public void notifyEndingSoon() {
        Instant now = Instant.now();
        int thresholdSeconds = notificationProperties.getSessionEndingSoonThresholdSeconds();
        for (Session session : sessionRepository.findAllByStatus(SessionStatus.ACTIVE)) {
            long remainingSeconds = Duration.between(now, session.getEndsAt()).getSeconds();
            if (remainingSeconds <= 0 || remainingSeconds > thresholdSeconds) {
                continue;
            }
            String warnedKey = WARNED_KEY_PREFIX + session.getId();
            boolean firstWarning = Boolean.TRUE.equals(
                    redisTemplate.opsForValue().setIfAbsent(warnedKey, "1", Duration.ofSeconds(thresholdSeconds)));
            if (firstWarning) {
                Match match = session.getMatch();
                notificationService.notifySessionEndingSoon(match.getUserOne(), session.getId(), remainingSeconds);
                notificationService.notifySessionEndingSoon(match.getUserTwo(), session.getId(), remainingSeconds);
            }
        }
    }
}
