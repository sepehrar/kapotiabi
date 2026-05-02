package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.JobRequestRequest;
import com.kapotiabi.dto.request.ShopOfferRequest;
import com.kapotiabi.dto.response.JobRequestResponse;
import com.kapotiabi.dto.response.ShopOfferResponse;
import com.kapotiabi.enums.JobRequestStatus;
import com.kapotiabi.enums.ShopOfferStatus;
import com.kapotiabi.exception.ForbiddenException;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.service.JobRequestService;
import com.kapotiabi.service.ShopOfferService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class JobRequestControllerTest {

  @Mock private JobRequestService jobRequestService;
  @Mock private ShopOfferService shopOfferService;

  @InjectMocks private JobRequestController jobRequestController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(jobRequestController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("5", null, List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private JobRequestResponse buildJobRequest() {
    return new JobRequestResponse(
        10L,
        5L,
        3L,
        "Replace brake pads",
        true,
        "OEM",
        "city",
        "Tallinn",
        "Harju",
        null,
        JobRequestStatus.PENDING,
        LocalDateTime.now().plusDays(1),
        LocalDateTime.now());
  }

  private ShopOfferResponse buildOffer() {
    return new ShopOfferResponse(
        20L,
        10L,
        2L,
        new BigDecimal("150.00"),
        "Pads + labor",
        "OEM",
        new BigDecimal("2.5"),
        LocalDateTime.now().plusDays(7),
        null,
        ShopOfferStatus.PENDING,
        LocalDateTime.now(),
        List.of());
  }

  @Test
  void getByUser_returns200() throws Exception {
    when(jobRequestService.getByUser(5L)).thenReturn(List.of(buildJobRequest()));

    mockMvc
        .perform(get("/api/v1/job-requests"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(10))
        .andExpect(jsonPath("$[0].userId").value(5));
  }

  @Test
  void getAvailable_passesCityAndCounty() throws Exception {
    Page<JobRequestResponse> page =
        new PageImpl<>(List.of(buildJobRequest()), PageRequest.of(0, 20), 1);
    when(jobRequestService.getAvailable(eq("Tallinn"), eq("Harju"), any(Pageable.class)))
        .thenReturn(page);

    mockMvc
        .perform(get("/api/v1/job-requests/available?city=Tallinn&county=Harju"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(10));

    verify(jobRequestService).getAvailable(eq("Tallinn"), eq("Harju"), any(Pageable.class));
  }

  @Test
  void create_returns201WithLocation() throws Exception {
    when(jobRequestService.create(any(JobRequestRequest.class), eq(5L)))
        .thenReturn(buildJobRequest());

    mockMvc
        .perform(
            post("/api/v1/job-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":3,\"description\":\"Replace brake pads\"}"))
        .andExpect(status().isCreated())
        .andExpect(
            header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/job-requests/10")))
        .andExpect(jsonPath("$.id").value(10));
  }

  @Test
  void create_missingCarId_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/job-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"description\":\"Replace brake pads\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("carId")));

    verifyNoInteractions(jobRequestService);
  }

  @Test
  void getById_notFound_returns404() throws Exception {
    when(jobRequestService.getById(99L)).thenThrow(new NotFoundException("Job request not found"));

    mockMvc
        .perform(get("/api/v1/job-requests/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Job request not found"));
  }

  @Test
  void update_returns200() throws Exception {
    when(jobRequestService.update(eq(10L), any(JobRequestRequest.class), eq(5L)))
        .thenReturn(buildJobRequest());

    mockMvc
        .perform(
            put("/api/v1/job-requests/10")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"carId\":3,\"description\":\"Replace brake pads\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(10));
  }

  @Test
  void cancel_returns200() throws Exception {
    when(jobRequestService.cancel(10L, 5L)).thenReturn(buildJobRequest());

    mockMvc.perform(delete("/api/v1/job-requests/10")).andExpect(status().isOk());

    verify(jobRequestService).cancel(10L, 5L);
  }

  @Test
  void getOffers_returns200() throws Exception {
    when(shopOfferService.getByJobRequest(10L)).thenReturn(List.of(buildOffer()));

    mockMvc
        .perform(get("/api/v1/job-requests/10/offers"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(20));
  }

  @Test
  void createOffer_returns201() throws Exception {
    when(shopOfferService.create(eq(10L), any(ShopOfferRequest.class), eq(5L)))
        .thenReturn(buildOffer());

    mockMvc
        .perform(
            post("/api/v1/job-requests/10/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceEstimate\":150.00,\"breakdown\":\"Pads + labor\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(20))
        .andExpect(jsonPath("$.priceEstimate").value(150.00));
  }

  @Test
  void createOffer_nonShopOwner_returns403() throws Exception {
    when(shopOfferService.create(eq(10L), any(ShopOfferRequest.class), eq(5L)))
        .thenThrow(new ForbiddenException("Only shop owners can submit offers"));

    mockMvc
        .perform(
            post("/api/v1/job-requests/10/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceEstimate\":150.00}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Only shop owners can submit offers"));
  }
}
