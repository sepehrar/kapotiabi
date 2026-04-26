package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.dto.request.CarRequest;
import com.kapotiabi.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class CarCrudIT extends BaseIntegrationTest {

  @Test
  void carCrudWithIdorEnforcement() throws Exception {
    String tokenA = registerAndGetToken("car-a@example.com", "Alice");
    String tokenB = registerAndGetToken("car-b@example.com", "Bob");

    CarRequest car = new CarRequest("123ABC", "Toyota", "Corolla", 2018, "1.6L petrol");

    MvcResult createResult =
        mockMvc
            .perform(
                post("/api/v1/cars")
                    .header("Authorization", tokenA)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(car)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.plateNumber").value("123ABC"))
            .andReturn();

    long carId =
        objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

    mockMvc
        .perform(get("/api/v1/cars/" + carId).header("Authorization", tokenA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.make").value("Toyota"));

    mockMvc
        .perform(get("/api/v1/cars").header("Authorization", tokenA))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].id").value(carId));

    mockMvc
        .perform(delete("/api/v1/cars/" + carId).header("Authorization", tokenB))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(get("/api/v1/cars/" + carId).header("Authorization", tokenA))
        .andExpect(status().isOk());

    mockMvc
        .perform(delete("/api/v1/cars/" + carId).header("Authorization", tokenA))
        .andExpect(status().isNoContent());

    mockMvc
        .perform(get("/api/v1/cars/" + carId).header("Authorization", tokenA))
        .andExpect(status().isNotFound());
  }

  private String registerAndGetToken(String email, String firstName) throws Exception {
    RegisterRequest req = new RegisterRequest();
    req.setEmail(email);
    req.setPassword("password123");
    req.setFirstName(firstName);
    req.setLastName("Owner");
    req.setRole(UserRole.CAR_OWNER);

    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();

    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return "Bearer " + body.get("token").asString();
  }
}
