package com.wick.user.scheduler;

import com.wick.user.domain.VerificationStatus;
import com.wick.user.repository.UserRepository;
import com.wick.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class VerificationTimeoutScheduler {

    private static final Duration TIMEOUT = Duration.ofDays(7);

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 30 3 * * *")
    @Transactional
    public void rejectExpiredPendingVerifications() {
        Instant cutoff = Instant.now().minus(TIMEOUT);
        userRepository.findAllByVerificationStatusAndVerificationSubmittedAtBefore(
                VerificationStatus.PENDING, cutoff)
            .forEach(user -> {
                user.setVerificationStatus(VerificationStatus.REJECTED);
                userRepository.save(user);
                notificationService.notifyVerificationRejected(user);
            });
    }
}
