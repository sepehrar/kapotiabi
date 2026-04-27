package com.kapotiabi.config;

import com.kapotiabi.entity.UserEntity;
import com.kapotiabi.enums.UserRole;
import com.kapotiabi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Value("${app.admin.email}")
  private String adminEmail;

  @Value("${app.admin.password}")
  private String adminPassword;

  @Override
  public void run(ApplicationArguments args) {
    if (userRepository.existsByEmail(adminEmail)) {
      return;
    }
    UserEntity admin =
        UserEntity.builder()
            .email(adminEmail)
            .passwordHash(passwordEncoder.encode(adminPassword))
            .role(UserRole.ADMIN)
            .firstName("Admin")
            .lastName("Kapotiabi")
            .build();
    userRepository.save(admin);
  }
}
