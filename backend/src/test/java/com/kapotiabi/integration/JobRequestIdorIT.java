package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.dto.request.CarRequest;
import com.kapotiabi.dto.request.JobRequestRequest;
import com.kapotiabi.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class JobRequestIdorIT extends BaseIntegrationTest {

  @Test
  void updateJobRequestByNonOwner_returns403() throws Exception {
    String tokenA = registerAndGetToken("jr-a@example.com", "Alice", UserRole.CAR_OWNER);
    String tokenB = registerAndGetToken("jr-b@example.com", "Bob", UserRole.CAR_OWNER);

    long carId = createCarFor(tokenA);
    long jobRequestId = createJobRequestFor(tokenA, carId);

    JobRequestRequest req =
        new JobRequestRequest(carId, "tampered description", null, null, null, null, null, null);

    mockMvc
        .perform(
            put("/api/v1/job-requests/" + jobRequestId)
                .header("Authorization", tokenB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden());
  }

  @Test
  void cancelJobRequestByNonOwner_returns403() throws Exception {
    String tokenA = registerAndGetToken("jr-c@example.com", "Carol", UserRole.CAR_OWNER);
    String tokenB = registerAndGetToken("jr-d@example.com", "Dan", UserRole.CAR_OWNER);

    long carId = createCarFor(tokenA);
    long jobRequestId = createJobRequestFor(tokenA, carId);

    mockMvc
        .perform(delete("/api/v1/job-requests/" + jobRequestId).header("Authorization", tokenB))
        .andExpect(status().isForbidden());
  }

  private String registerAndGetToken(String email, String firstName, UserRole role)
      throws Exception {
    RegisterRequest req = new RegisterRequest();
    req.setEmail(email);
    req.setPassword("password123");
    req.setFirstName(firstName);
    req.setLastName("Test");
    req.setRole(role);

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

  private long createCarFor(String token) throws Exception {
    CarRequest car =
        new CarRequest("ABC" + System.nanoTime() % 100000, "Toyota", "Corolla", 2018, "1.6L");
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/cars")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(car)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }

  private long createJobRequestFor(String token, long carId) throws Exception {
    JobRequestRequest req =
        new JobRequestRequest(
            carId, "Brake pads need replacement", null, null, null, "Tallinn", "Harju", null);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/job-requests")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }
}
