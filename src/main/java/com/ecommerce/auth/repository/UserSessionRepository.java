package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    Optional<UserSession> findByJti(String jti);
    List<UserSession> findAllByUserAndRevokedAtIsNull(User user);
    void deleteByUser(User user);
}
