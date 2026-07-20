package com.wick.token.repository;

import com.wick.token.domain.TokenBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TokenBalanceRepository extends JpaRepository<TokenBalance, UUID> {

    Optional<TokenBalance> findByUserId(UUID userId);

    @Modifying(clearAutomatically = true)
    @Query("update TokenBalance t set t.balance = t.balance - :amount where t.user.id = :userId and t.balance >= :amount")
    int debit(@Param("userId") UUID userId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query("update TokenBalance t set t.balance = t.balance + :amount where t.user.id = :userId")
    int credit(@Param("userId") UUID userId, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query(value = "insert into token_balances (id, user_id, balance, created_at, updated_at) "
            + "values (:id, :userId, 0, now(), now()) "
            + "on conflict (user_id) do nothing", nativeQuery = true)
    int insertIfAbsent(@Param("id") UUID id, @Param("userId") UUID userId);
}
