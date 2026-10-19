package com.ecommerce.auth.repository;

import com.ecommerce.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByMobileE164(String mobileE164);
    Optional<User> findByEmailOrMobileE164(String email, String mobileE164);
    boolean existsByEmail(String email);
    boolean existsByMobileE164(String mobileE164);
}
