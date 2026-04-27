package com.kapotiabi.config;

import com.kapotiabi.entity.UserEntity;
import com.kapotiabi.enums.UserRole;
import com.kapotiabi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.admin.seed-enabled", havingValue = "true")
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
    if (adminPassword == null || adminPassword.isBlank()) {
      throw new IllegalStateException(
          "app.admin.seed-enabled is true but app.admin.password is blank. "
              + "Set ADMIN_DEFAULT_PASSWORD to a non-empty value.");
    }
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
