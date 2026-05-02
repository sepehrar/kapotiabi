package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.GuestEstimateRequest;
import com.kapotiabi.dto.response.GuestEstimateResponse;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.service.GuestEstimateService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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
class GuestEstimateControllerTest {

  @Mock private GuestEstimateService guestEstimateService;

  @InjectMocks private GuestEstimateController guestEstimateController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(guestEstimateController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  private GuestEstimateResponse buildResponse() {
    return new GuestEstimateResponse(
        7L,
        "Brake pads need replacement",
        "Tallinn",
        "Harju",
        new BigDecimal("100.00"),
        new BigDecimal("250.00"),
        LocalDateTime.now(),
        List.of());
  }

  @Test
  void create_returns201WithLocation() throws Exception {
    when(guestEstimateService.create(any(GuestEstimateRequest.class))).thenReturn(buildResponse());

    mockMvc
        .perform(
            post("/api/v1/guest-estimates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Brake pads\",\"city\":\"Tallinn\"}"))
        .andExpect(status().isCreated())
        .andExpect(
            header()
                .string("Location", org.hamcrest.Matchers.endsWith("/api/v1/guest-estimates/7")))
        .andExpect(jsonPath("$.id").value(7));
  }

  @Test
  void getById_returns200() throws Exception {
    when(guestEstimateService.getById(7L)).thenReturn(buildResponse());

    mockMvc
        .perform(get("/api/v1/guest-estimates/7"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(7))
        .andExpect(jsonPath("$.priceFrom").value(100.00));
  }

  @Test
  void getById_notFound_returns404() throws Exception {
    when(guestEstimateService.getById(99L))
        .thenThrow(new NotFoundException("Guest estimate not found: 99"));

    mockMvc
        .perform(get("/api/v1/guest-estimates/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Guest estimate not found: 99"));
  }

  @Test
  void create_blankDescription_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/guest-estimates")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"\",\"city\":\"Tallinn\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("description")));

    verifyNoInteractions(guestEstimateService);
  }
}
