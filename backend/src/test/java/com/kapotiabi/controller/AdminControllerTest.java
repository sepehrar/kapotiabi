package com.kapotiabi.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.response.RepairShopResponse;
import com.kapotiabi.enums.RepairShopStatus;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.service.RepairShopService;
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
class AdminControllerTest {

  @Mock private RepairShopService repairShopService;

  @InjectMocks private AdminController adminController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(adminController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
  }

  private RepairShopResponse buildResponse(RepairShopStatus status) {
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
        status,
        LocalDateTime.now());
  }

  @Test
  void listShops_passesStatusFilter() throws Exception {
    when(repairShopService.getAllForAdmin("draft"))
        .thenReturn(List.of(buildResponse(RepairShopStatus.DRAFT)));

    mockMvc
        .perform(get("/api/v1/admin/shops?status=draft"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].status").value("draft"));

    verify(repairShopService).getAllForAdmin("draft");
  }

  @Test
  void listShops_noFilter_passesNull() throws Exception {
    when(repairShopService.getAllForAdmin(null)).thenReturn(List.of());

    mockMvc.perform(get("/api/v1/admin/shops")).andExpect(status().isOk());

    verify(repairShopService).getAllForAdmin(null);
  }

  @Test
  void updateShopStatus_approve_returns200() throws Exception {
    when(repairShopService.updateShopStatus(1L, "approve"))
        .thenReturn(buildResponse(RepairShopStatus.ACTIVE));

    mockMvc
        .perform(
            patch("/api/v1/admin/shops/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"approve\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("active"));
  }

  @Test
  void updateShopStatus_invalidAction_returns400() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/admin/shops/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"action\":\"foo\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("action")));

    verifyNoInteractions(repairShopService);
  }
}
