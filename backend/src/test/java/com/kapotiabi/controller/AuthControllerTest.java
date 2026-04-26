package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.*;
import com.kapotiabi.enums.RepairShopStatus;
import com.kapotiabi.exception.*;
import com.kapotiabi.service.AuthService;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;

  @InjectMocks private AuthController authController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  private static final AuthResponse CAR_OWNER_RESPONSE =
      AuthResponse.builder()
          .token("eyJ.dummy.token")
          .expiresAt(Instant.parse("2026-04-07T10:00:00Z"))
          .role("car_owner")
          .userId(1L)
          .shopStatus(null)
          .build();

  // ── Register ─────────────────────────────────────────────────────────────

  @Test
  void register_carOwner_returns201() throws Exception {
    when(authService.register(any())).thenReturn(CAR_OWNER_RESPONSE);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"user@test.ee","password":"Test123",
                                 "firstName":"Mari","lastName":"Mets","role":"CAR_OWNER"}
                                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.token").value("eyJ.dummy.token"))
        .andExpect(jsonPath("$.role").value("car_owner"))
        .andExpect(jsonPath("$.shopStatus").doesNotExist());
  }

  @Test
  void register_shopOwner_returns201WithShopStatus() throws Exception {
    AuthResponse shopResponse =
        AuthResponse.builder()
            .token("eyJ.shop.token")
            .expiresAt(Instant.parse("2026-04-07T10:00:00Z"))
            .role("shop_owner")
            .userId(2L)
            .shopStatus(RepairShopStatus.DRAFT)
            .build();
    when(authService.register(any())).thenReturn(shopResponse);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"shop@test.ee","password":"Test123",
                                 "firstName":"Jaan","lastName":"Tamm","role":"SHOP_OWNER"}
                                """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.shopStatus").value("draft"));
  }

  @Test
  void register_duplicateEmail_returns409() throws Exception {
    when(authService.register(any())).thenThrow(new DuplicateEmailException());

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"user@test.ee","password":"Test123",
                                 "firstName":"Mari","lastName":"Mets","role":"CAR_OWNER"}
                                """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.message").value("Email already registered"));
  }

  @Test
  void register_adminRole_returns400() throws Exception {
    when(authService.register(any()))
        .thenThrow(new IllegalArgumentException("Cannot self-register as admin"));

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"hack@test.ee","password":"Test123",
                                 "firstName":"Bad","lastName":"Actor","role":"ADMIN"}
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Cannot self-register as admin"));
  }

  @Test
  void register_missingEmail_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"password":"Test123","firstName":"Mari","lastName":"Mets","role":"CAR_OWNER"}
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_shortPassword_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"user@test.ee","password":"abc",
                                 "firstName":"Mari","lastName":"Mets","role":"CAR_OWNER"}
                                """))
        .andExpect(status().isBadRequest());
  }

  @Test
  void register_invalidEmail_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"not-an-email","password":"Test123",
                                 "firstName":"Mari","lastName":"Mets","role":"CAR_OWNER"}
                                """))
        .andExpect(status().isBadRequest());
  }

  // ── Login ─────────────────────────────────────────────────────────────────

  @Test
  void login_validCredentials_returns200() throws Exception {
    when(authService.login(any())).thenReturn(CAR_OWNER_RESPONSE);

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"user@test.ee","password":"Test123"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.token").exists());
  }

  @Test
  void login_wrongPassword_returns401() throws Exception {
    when(authService.login(any())).thenThrow(new InvalidCredentialsException());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"user@test.ee","password":"wrong"}
                                """))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.message").value("Invalid email or password"));
  }

  @Test
  void login_unknownEmail_returns401() throws Exception {
    when(authService.login(any())).thenThrow(new InvalidCredentialsException());

    mockMvc
        .perform(
            post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"nobody@test.ee","password":"Test123"}
                                """))
        .andExpect(status().isUnauthorized());
  }

  // ── Forgot password ───────────────────────────────────────────────────────

  @Test
  void forgotPassword_existingEmail_returns200WithGenericMessage() throws Exception {
    when(authService.forgotPassword(any()))
        .thenReturn(Map.of("message", "If that email exists, a reset link has been sent."));

    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"user@test.ee"}
                                """))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message").value("If that email exists, a reset link has been sent."));
  }

  @Test
  void forgotPassword_unknownEmail_returns200SameMessage() throws Exception {
    when(authService.forgotPassword(any()))
        .thenReturn(Map.of("message", "If that email exists, a reset link has been sent."));

    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"nobody@test.ee"}
                                """))
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.message").value("If that email exists, a reset link has been sent."));
  }

  @Test
  void forgotPassword_invalidEmail_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"email":"not-an-email"}
                                """))
        .andExpect(status().isBadRequest());
  }

  // ── Reset password ────────────────────────────────────────────────────────

  @Test
  void resetPassword_validToken_returns200() throws Exception {
    when(authService.resetPassword(any()))
        .thenReturn(Map.of("message", "Password updated successfully."));

    mockMvc
        .perform(
            post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"token":"valid-uuid","newPassword":"NewPass456"}
                                """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.message").value("Password updated successfully."));
  }

  @Test
  void resetPassword_expiredToken_returns400() throws Exception {
    when(authService.resetPassword(any()))
        .thenThrow(new InvalidTokenException("Reset token has expired"));

    mockMvc
        .perform(
            post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"token":"expired-uuid","newPassword":"NewPass456"}
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Reset token has expired"));
  }

  @Test
  void resetPassword_usedToken_returns400() throws Exception {
    when(authService.resetPassword(any()))
        .thenThrow(new InvalidTokenException("Reset token has already been used"));

    mockMvc
        .perform(
            post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"token":"used-uuid","newPassword":"NewPass456"}
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Reset token has already been used"));
  }

  @Test
  void resetPassword_unknownToken_returns400() throws Exception {
    when(authService.resetPassword(any()))
        .thenThrow(new InvalidTokenException("Invalid or unknown reset token"));

    mockMvc
        .perform(
            post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"token":"unknown-uuid","newPassword":"NewPass456"}
                                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value("Invalid or unknown reset token"));
  }

  @Test
  void resetPassword_shortPassword_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                                {"token":"some-uuid","newPassword":"abc"}
                                """))
        .andExpect(status().isBadRequest());
  }
}
