package com.wick.payment.repository;

import com.wick.payment.domain.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseRepository extends JpaRepository<Purchase, UUID> {

    boolean existsByPurchaseToken(String purchaseToken);

    Optional<Purchase> findByPurchaseToken(String purchaseToken);

    List<Purchase> findAllByUserId(UUID userId);
}
