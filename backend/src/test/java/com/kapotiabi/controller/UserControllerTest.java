package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.UpdateUserRequest;
import com.kapotiabi.dto.response.UserResponse;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.service.UserService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

  @Mock private UserService userService;

  @InjectMocks private UserController userController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(userController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("5", null, List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private UserResponse buildResponse() {
    return new UserResponse(
        5L,
        "user@test.ee",
        "car_owner",
        "Mari",
        "Mets",
        "+372 555",
        "Tallinn",
        50,
        null,
        LocalDateTime.now());
  }

  @Test
  void getMe_returns200() throws Exception {
    when(userService.getById(5L)).thenReturn(buildResponse());

    mockMvc
        .perform(get("/api/v1/users/me"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(5))
        .andExpect(jsonPath("$.email").value("user@test.ee"))
        .andExpect(jsonPath("$.firstName").value("Mari"));
  }

  @Test
  void updateMe_returns200() throws Exception {
    when(userService.update(eq(5L), any(UpdateUserRequest.class))).thenReturn(buildResponse());

    mockMvc
        .perform(
            put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    "{\"firstName\":\"Mari\",\"lastName\":\"Mets\",\"preferredCity\":\"Tallinn\",\"searchRadiusKm\":50}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstName").value("Mari"));
  }

  @Test
  void updateMe_blankFirstName_returns400() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/users/me")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"\",\"lastName\":\"Mets\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("firstName")));

    verifyNoInteractions(userService);
  }
}
