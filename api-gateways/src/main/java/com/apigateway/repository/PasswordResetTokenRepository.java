package com.apigateway.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apigateway.model.PasswordResetToken;
import com.apigateway.model.User;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
