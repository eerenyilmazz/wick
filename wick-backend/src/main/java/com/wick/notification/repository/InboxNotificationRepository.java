package com.wick.notification.repository;

import com.wick.notification.domain.InboxNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface InboxNotificationRepository extends JpaRepository<InboxNotification, UUID> {

    Page<InboxNotification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

    @Modifying
    @Query("UPDATE InboxNotification n SET n.read = true WHERE n.user.id = :userId")
    void markAllReadByUserId(UUID userId);
}
