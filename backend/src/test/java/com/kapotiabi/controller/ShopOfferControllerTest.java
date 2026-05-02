package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.ShopOfferRequest;
import com.kapotiabi.dto.response.BookingResponse;
import com.kapotiabi.dto.response.ShopOfferResponse;
import com.kapotiabi.enums.BookingStatus;
import com.kapotiabi.enums.ShopOfferStatus;
import com.kapotiabi.exception.ForbiddenException;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.service.BookingService;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ShopOfferControllerTest {

  @Mock private ShopOfferService shopOfferService;
  @Mock private BookingService bookingService;

  @InjectMocks private ShopOfferController shopOfferController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(shopOfferController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("5", null, List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private ShopOfferResponse buildOffer(ShopOfferStatus status) {
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
        status,
        LocalDateTime.now(),
        List.of());
  }

  private BookingResponse buildBooking() {
    return new BookingResponse(
        100L,
        20L,
        10L,
        5L,
        2L,
        LocalDateTime.now().plusDays(2),
        Boolean.FALSE,
        null,
        BookingStatus.AWAITING_SHOP,
        LocalDateTime.now());
  }

  @Test
  void getById_returns200() throws Exception {
    when(shopOfferService.getById(20L)).thenReturn(buildOffer(ShopOfferStatus.PENDING));

    mockMvc
        .perform(get("/api/v1/offers/20"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(20));
  }

  @Test
  void update_returns200() throws Exception {
    when(shopOfferService.update(eq(20L), any(ShopOfferRequest.class), eq(5L)))
        .thenReturn(buildOffer(ShopOfferStatus.PENDING));

    mockMvc
        .perform(
            put("/api/v1/offers/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceEstimate\":150.00}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(20));
  }

  @Test
  void update_nonOwner_returns403() throws Exception {
    when(shopOfferService.update(eq(20L), any(ShopOfferRequest.class), eq(5L)))
        .thenThrow(new ForbiddenException("Not your offer"));

    mockMvc
        .perform(
            put("/api/v1/offers/20")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"priceEstimate\":150.00}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Not your offer"));
  }

  @Test
  void accept_returns200WithBooking() throws Exception {
    when(bookingService.createFromOffer(20L, 7L, 5L)).thenReturn(buildBooking());

    mockMvc
        .perform(
            post("/api/v1/offers/20/accept")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"slotId\":7}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100));
  }

  @Test
  void accept_missingSlotId_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/offers/20/accept").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("slotId")));

    verifyNoInteractions(bookingService);
  }

  @Test
  void reject_returns200() throws Exception {
    when(shopOfferService.reject(20L, 5L)).thenReturn(buildOffer(ShopOfferStatus.REJECTED));

    mockMvc
        .perform(post("/api/v1/offers/20/reject"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("rejected"));
  }

  @Test
  void uploadPdf_returns200() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile(
            "file", "offer.pdf", MediaType.APPLICATION_PDF_VALUE, "%PDF-1.4 dummy".getBytes());

    mockMvc.perform(multipart("/api/v1/offers/20/pdf").file(file)).andExpect(status().isOk());

    verify(shopOfferService).uploadPdf(eq(20L), any(), eq(5L));
  }

  @Test
  void downloadPdf_returns200WithPdfContent() throws Exception {
    Resource resource = new ByteArrayResource("%PDF-1.4 dummy".getBytes());
    when(shopOfferService.downloadPdf(20L)).thenReturn(resource);

    mockMvc
        .perform(get("/api/v1/offers/20/pdf"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(
            header()
                .string(
                    "Content-Disposition", org.hamcrest.Matchers.containsString("offer_20.pdf")));
  }
}
