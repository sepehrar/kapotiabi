package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.RepairShopRequest;
import com.kapotiabi.dto.request.ShopServiceCategoryRequest;
import com.kapotiabi.dto.response.RepairShopResponse;
import com.kapotiabi.dto.response.ReviewResponse;
import com.kapotiabi.dto.response.ShopServiceCategoryResponse;
import com.kapotiabi.enums.RepairShopStatus;
import com.kapotiabi.exception.ForbiddenException;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.service.BookingService;
import com.kapotiabi.service.RepairShopService;
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
class RepairShopControllerTest {

  @Mock private RepairShopService repairShopService;
  @Mock private ShopOfferService shopOfferService;
  @Mock private BookingService bookingService;

  @InjectMocks private RepairShopController repairShopController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(repairShopController)
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

  private RepairShopResponse buildShop() {
    return new RepairShopResponse(
        1L,
        2L,
        "AutoExpert",
        "Tartu mnt 1",
        "Tallinn",
        "Harju",
        "+372 555",
        "shop@test.ee",
        null,
        null,
        RepairShopStatus.ACTIVE,
        LocalDateTime.now());
  }

  private ShopServiceCategoryResponse buildCategory() {
    return new ShopServiceCategoryResponse(
        30L,
        1L,
        "Brakes",
        new BigDecimal("50.00"),
        new BigDecimal("200.00"),
        "OEM",
        LocalDateTime.now());
  }

  @Test
  void getAll_passesFiltersAndReturnsPage() throws Exception {
    Page<RepairShopResponse> page = new PageImpl<>(List.of(buildShop()), PageRequest.of(0, 20), 1);
    when(repairShopService.getAll(eq("Tallinn"), eq("Harju"), any(Pageable.class)))
        .thenReturn(page);

    mockMvc
        .perform(get("/api/v1/shops?city=Tallinn&county=Harju"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content[0].id").value(1));
  }

  @Test
  void create_returns201WithLocation() throws Exception {
    when(repairShopService.create(any(RepairShopRequest.class), eq(5L))).thenReturn(buildShop());

    mockMvc
        .perform(
            post("/api/v1/shops")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"AutoExpert\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/shops/1")))
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void create_blankName_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/shops")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")));

    verifyNoInteractions(repairShopService);
  }

  @Test
  void getById_returns200() throws Exception {
    when(repairShopService.getById(1L)).thenReturn(buildShop());

    mockMvc.perform(get("/api/v1/shops/1")).andExpect(status().isOk());
  }

  @Test
  void update_nonOwner_returns403() throws Exception {
    when(repairShopService.update(eq(1L), any(RepairShopRequest.class), eq(5L)))
        .thenThrow(new ForbiddenException("Not your shop"));

    mockMvc
        .perform(
            put("/api/v1/shops/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"AutoExpert\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Not your shop"));
  }

  @Test
  void addCategory_returns201() throws Exception {
    when(repairShopService.addCategory(eq(1L), any(ShopServiceCategoryRequest.class), eq(5L)))
        .thenReturn(buildCategory());

    mockMvc
        .perform(
            post("/api/v1/shops/1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Brakes\",\"priceFrom\":50.00,\"priceTo\":200.00}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(30))
        .andExpect(jsonPath("$.category").value("Brakes"));
  }

  @Test
  void addCategory_nonOwner_returns403() throws Exception {
    when(repairShopService.addCategory(eq(1L), any(ShopServiceCategoryRequest.class), eq(5L)))
        .thenThrow(new ForbiddenException("Not your shop"));

    mockMvc
        .perform(
            post("/api/v1/shops/1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"category\":\"Brakes\",\"priceFrom\":50.00,\"priceTo\":200.00}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Not your shop"));
  }

  @Test
  void deleteCategory_returns204() throws Exception {
    doNothing().when(repairShopService).deleteCategory(1L, 30L, 5L);

    mockMvc.perform(delete("/api/v1/shops/1/categories/30")).andExpect(status().isNoContent());

    verify(repairShopService).deleteCategory(1L, 30L, 5L);
  }

  @Test
  void deleteCategory_nonOwner_returns403() throws Exception {
    doThrow(new ForbiddenException("Not your shop"))
        .when(repairShopService)
        .deleteCategory(1L, 30L, 5L);

    mockMvc
        .perform(delete("/api/v1/shops/1/categories/30"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.message").value("Not your shop"));
  }

  @Test
  void getReviews_returns200() throws Exception {
    when(repairShopService.getReviews(1L))
        .thenReturn(
            List.of(new ReviewResponse(50L, 100L, 5L, 1L, 5, "Great", LocalDateTime.now())));

    mockMvc
        .perform(get("/api/v1/shops/1/reviews"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(50));
  }
}
