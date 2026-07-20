package com.wick.user.repository;

import com.wick.user.domain.DiscoveryPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DiscoveryPreferenceRepository extends JpaRepository<DiscoveryPreference, UUID> {

    Optional<DiscoveryPreference> findByUserId(UUID userId);
}
