package com.kapotiabi.repository;

import com.kapotiabi.entity.PasswordResetTokenEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository
    extends JpaRepository<PasswordResetTokenEntity, Integer> {
  Optional<PasswordResetTokenEntity> findByToken(String token);
}
