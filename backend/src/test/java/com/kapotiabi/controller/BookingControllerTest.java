package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.ReviewRequest;
import com.kapotiabi.dto.response.BookingResponse;
import com.kapotiabi.dto.response.ReviewResponse;
import com.kapotiabi.enums.BookingStatus;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.service.BookingService;
import com.kapotiabi.service.ReviewService;
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
class BookingControllerTest {

  @Mock private BookingService bookingService;
  @Mock private ReviewService reviewService;

  @InjectMocks private BookingController bookingController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(bookingController)
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

  private BookingResponse buildBooking(BookingStatus status) {
    return new BookingResponse(
        100L,
        20L,
        10L,
        5L,
        2L,
        LocalDateTime.now().plusDays(2),
        Boolean.TRUE,
        LocalDateTime.now(),
        status,
        LocalDateTime.now());
  }

  private ReviewResponse buildReview() {
    return new ReviewResponse(50L, 100L, 5L, 2L, 5, "Great service", LocalDateTime.now());
  }

  @Test
  void getBookings_returnsPaged() throws Exception {
    Page<BookingResponse> page =
        new PageImpl<>(
            List.of(buildBooking(BookingStatus.AWAITING_SHOP)), PageRequest.of(0, 20), 1);
    when(bookingService.getByUser(eq(5L), any(Pageable.class))).thenReturn(page);

    mockMvc
        .perform(get("/api/v1/bookings"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(100));
  }

  @Test
  void getById_returns200() throws Exception {
    when(bookingService.getById(100L)).thenReturn(buildBooking(BookingStatus.AWAITING_SHOP));

    mockMvc
        .perform(get("/api/v1/bookings/100"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100));
  }

  @Test
  void getById_notFound_returns404() throws Exception {
    when(bookingService.getById(999L)).thenThrow(new NotFoundException("Booking not found"));

    mockMvc.perform(get("/api/v1/bookings/999")).andExpect(status().isNotFound());
  }

  @Test
  void confirm_returns200() throws Exception {
    when(bookingService.confirm(100L, 5L)).thenReturn(buildBooking(BookingStatus.CONFIRMED));

    mockMvc
        .perform(post("/api/v1/bookings/100/confirm"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("confirmed"));
  }

  @Test
  void schedule_passesScheduledAtAndReturns200() throws Exception {
    when(bookingService.schedule(eq(100L), any(LocalDateTime.class), eq(5L)))
        .thenReturn(buildBooking(BookingStatus.CONFIRMED));

    mockMvc
        .perform(post("/api/v1/bookings/100/schedule?scheduledAt=2026-06-01T10:00:00"))
        .andExpect(status().isOk());

    verify(bookingService).schedule(eq(100L), any(LocalDateTime.class), eq(5L));
  }

  @Test
  void complete_returns200() throws Exception {
    when(bookingService.complete(100L, 5L)).thenReturn(buildBooking(BookingStatus.COMPLETED));

    mockMvc
        .perform(post("/api/v1/bookings/100/complete"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("completed"));
  }

  @Test
  void cancel_returns200() throws Exception {
    when(bookingService.cancel(100L, 5L)).thenReturn(buildBooking(BookingStatus.CANCELLED));

    mockMvc
        .perform(post("/api/v1/bookings/100/cancel"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("cancelled"));
  }

  @Test
  void submitReview_returns200() throws Exception {
    when(reviewService.submit(eq(100L), any(ReviewRequest.class), eq(5L)))
        .thenReturn(buildReview());

    mockMvc
        .perform(
            post("/api/v1/bookings/100/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":5,\"comment\":\"Great service\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(50))
        .andExpect(jsonPath("$.rating").value(5));
  }

  @Test
  void submitReview_invalidRating_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/bookings/100/review")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"rating\":7,\"comment\":\"Too high\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("rating")));

    verifyNoInteractions(reviewService);
  }

  @Test
  void getReview_returns200() throws Exception {
    when(reviewService.getByBooking(100L)).thenReturn(buildReview());

    mockMvc
        .perform(get("/api/v1/bookings/100/review"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(50));
  }
}
