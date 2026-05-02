package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.enums.UserRole;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultMatcher;

class SecurityFilterChainIT extends BaseIntegrationTest {

  @Value("${jwt.secret}")
  private String jwtSecret;

  private static final String OTHER_SECRET =
      "another-integration-secret-not-the-real-one-padding-padding-padding";

  private SecretKey realKey() {
    return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
  }

  private SecretKey wrongKey() {
    return Keys.hmacShaKeyFor(OTHER_SECRET.getBytes(StandardCharsets.UTF_8));
  }

  private String buildToken(SecretKey key, Instant expiry, String role) {
    Instant now = Instant.now();
    return Jwts.builder()
        .subject("1")
        .claim("email", "test@example.com")
        .claim("role", role)
        .issuedAt(Date.from(now))
        .expiration(Date.from(expiry))
        .signWith(key)
        .compact();
  }

  private static final ResultMatcher IS_AUTH_ERROR = status().is4xxClientError();

  @Test
  void protectedEndpointWithoutAuthHeader_returns4xx() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(IS_AUTH_ERROR);
  }

  @Test
  void protectedEndpointWithEmptyBearer_returns4xx() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer "))
        .andExpect(IS_AUTH_ERROR);
  }

  @Test
  void protectedEndpointWithBasicScheme_returns4xx() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Basic dXNlcjpwYXNz"))
        .andExpect(IS_AUTH_ERROR);
  }

  @Test
  void protectedEndpointWithExpiredJwt_returns4xx() throws Exception {
    String expired = buildToken(realKey(), Instant.now().minusSeconds(3600), "car_owner");

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + expired))
        .andExpect(IS_AUTH_ERROR);
  }

  @Test
  void protectedEndpointWithTamperedSignature_returns4xx() throws Exception {
    String valid = buildToken(realKey(), Instant.now().plusSeconds(3600), "car_owner");
    String tampered = valid.substring(0, valid.length() - 1) + (valid.endsWith("A") ? "B" : "A");

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + tampered))
        .andExpect(IS_AUTH_ERROR);
  }

  @Test
  void protectedEndpointWithJwtSignedByWrongKey_returns4xx() throws Exception {
    String wrong = buildToken(wrongKey(), Instant.now().plusSeconds(3600), "car_owner");

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + wrong))
        .andExpect(IS_AUTH_ERROR);
  }

  @Test
  void adminEndpointRejectsCarOwnerRoleWith403() throws Exception {
    String carOwnerToken = bearerFor(1L, "carowner@example.com", "CAR_OWNER");

    mockMvc
        .perform(get("/api/v1/admin/shops").header("Authorization", carOwnerToken))
        .andExpect(status().isForbidden());
  }

  @Test
  void adminEndpointAcceptsAdminRole_isNot401or403() throws Exception {
    String adminToken = bearerFor(2L, "admin@example.com", "ADMIN");

    mockMvc
        .perform(get("/api/v1/admin/shops").header("Authorization", adminToken))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              if (status == 401 || status == 403) {
                throw new AssertionError(
                    "Expected admin role to pass auth gate, got status " + status);
              }
            });
  }

  @Test
  void authRegisterIsPublic_noToken() throws Exception {
    RegisterRequest req = new RegisterRequest();
    req.setEmail("public-register@example.com");
    req.setPassword("password123");
    req.setFirstName("Public");
    req.setLastName("Register");
    req.setRole(UserRole.CAR_OWNER);

    mockMvc
        .perform(
            post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated());
  }

  @Test
  void swaggerOpenApiPathRequiresAuth_noToken() throws Exception {
    mockMvc.perform(get("/v3/api-docs")).andExpect(IS_AUTH_ERROR);
  }

  @Test
  void swaggerUiPathRequiresAuth_noToken() throws Exception {
    mockMvc.perform(get("/swagger-ui/index.html")).andExpect(IS_AUTH_ERROR);
  }
}
