package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.LoginRequest;
import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class AuthFlowIT extends BaseIntegrationTest {

  @Test
  void registerThenLoginThenAccessProtectedEndpoint() throws Exception {
    RegisterRequest register = new RegisterRequest();
    register.setEmail("auth-flow@example.com");
    register.setPassword("password123");
    register.setFirstName("Auth");
    register.setLastName("Flow");
    register.setRole(UserRole.CAR_OWNER);

    MvcResult registerResult =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(register)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(jsonPath("$.userId").isNumber())
            .andReturn();

    JsonNode registerBody =
        objectMapper.readTree(registerResult.getResponse().getContentAsString());
    String registerToken = registerBody.get("token").asString();

    LoginRequest login = new LoginRequest();
    login.setEmail("auth-flow@example.com");
    login.setPassword("password123");

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andReturn();

    String loginToken =
        objectMapper
            .readTree(loginResult.getResponse().getContentAsString())
            .get("token")
            .asString();

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + loginToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("auth-flow@example.com"));

    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer " + registerToken))
        .andExpect(status().isOk());
  }

  @Test
  void protectedEndpointRejectsRequestWithoutToken() throws Exception {
    mockMvc.perform(get("/api/v1/users/me")).andExpect(status().is4xxClientError());
  }

  @Test
  void protectedEndpointRejectsRequestWithGarbageToken() throws Exception {
    mockMvc
        .perform(get("/api/v1/users/me").header("Authorization", "Bearer not-a-real-jwt"))
        .andExpect(status().is4xxClientError());
  }
}
