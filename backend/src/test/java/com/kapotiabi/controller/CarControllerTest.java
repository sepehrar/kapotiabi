package com.kapotiabi.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.kapotiabi.dto.request.CarRequest;
import com.kapotiabi.dto.response.CarResponse;
import com.kapotiabi.exception.GlobalExceptionHandler;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.service.CarService;
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
class CarControllerTest {

  @Mock private CarService carService;

  @InjectMocks private CarController carController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc =
        MockMvcBuilders.standaloneSetup(carController)
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("5", null, List.of()));
  }

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  private CarResponse buildResponse() {
    return new CarResponse(1L, 5L, "123ABC", "Toyota", "Corolla", 2018, "1.6", LocalDateTime.now());
  }

  @Test
  void getByOwner_returns200WithList() throws Exception {
    when(carService.getByOwner(5L)).thenReturn(List.of(buildResponse()));

    mockMvc
        .perform(get("/api/v1/cars"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].plateNumber").value("123ABC"))
        .andExpect(jsonPath("$[0].make").value("Toyota"));
  }

  @Test
  void create_returns201WithLocation() throws Exception {
    when(carService.create(any(CarRequest.class), eq(5L))).thenReturn(buildResponse());

    mockMvc
        .perform(
            post("/api/v1/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plateNumber\":\"123ABC\",\"make\":\"Toyota\"}"))
        .andExpect(status().isCreated())
        .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/api/v1/cars/1")))
        .andExpect(jsonPath("$.id").value(1))
        .andExpect(jsonPath("$.plateNumber").value("123ABC"));
  }

  @Test
  void create_blankPlateNumber_returns400() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/cars")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plateNumber\":\"\",\"make\":\"Toyota\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.message").value(org.hamcrest.Matchers.containsString("plateNumber")));

    verifyNoInteractions(carService);
  }

  @Test
  void getById_returns200() throws Exception {
    when(carService.getById(1L)).thenReturn(buildResponse());

    mockMvc
        .perform(get("/api/v1/cars/1"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void getById_notFound_returns404() throws Exception {
    when(carService.getById(99L)).thenThrow(new NotFoundException("Car not found: 99"));

    mockMvc
        .perform(get("/api/v1/cars/99"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.message").value("Car not found: 99"));
  }

  @Test
  void update_returns200() throws Exception {
    when(carService.update(eq(1L), any(CarRequest.class), eq(5L))).thenReturn(buildResponse());

    mockMvc
        .perform(
            put("/api/v1/cars/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"plateNumber\":\"123ABC\",\"make\":\"Toyota\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1));
  }

  @Test
  void delete_returns204() throws Exception {
    doNothing().when(carService).delete(1L, 5L);

    mockMvc.perform(delete("/api/v1/cars/1")).andExpect(status().isNoContent());

    verify(carService).delete(1L, 5L);
  }
}
