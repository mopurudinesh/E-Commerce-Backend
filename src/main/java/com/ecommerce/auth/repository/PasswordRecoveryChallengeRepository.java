package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.PasswordRecoveryChallenge;
import com.ecommerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordRecoveryChallengeRepository extends JpaRepository<PasswordRecoveryChallenge, Long> {
    Optional<PasswordRecoveryChallenge> findTopByUserOrderByCreatedAtDesc(User user);
    Optional<PasswordRecoveryChallenge> findByRecoveryTokenHash(String recoveryTokenHash);
    void deleteByUser(User user);
}
