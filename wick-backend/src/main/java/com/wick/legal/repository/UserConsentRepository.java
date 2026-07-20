package com.wick.legal.repository;

import com.wick.legal.domain.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserConsentRepository extends JpaRepository<UserConsent, UUID> {
}
