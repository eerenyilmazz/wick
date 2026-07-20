package com.wick.user.scheduler;

import com.wick.user.domain.User;
import com.wick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCleanupScheduler {

    private static final Duration REACTIVATION_WINDOW = Duration.ofDays(3);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void hardDeleteExpiredAccounts() {
        Instant cutoff = Instant.now().minus(REACTIVATION_WINDOW);
        List<User> expired = userRepository.findAllByDeletedAtIsNotNullAndDeletedAtBefore(cutoff);
        if (expired.isEmpty()) return;

        for (User user : expired) {
            user.setEmail("deleted-" + user.getId() + "@wick.invalid");
            user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
            user.setDisplayName("Deleted User");
            user.setBio(null);
        }
        userRepository.saveAll(expired);
        log.info("Hard-deleted {} expired accounts", expired.size());
    }
}
