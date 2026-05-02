package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.dto.request.AcceptOfferRequest;
import com.kapotiabi.dto.request.CarRequest;
import com.kapotiabi.dto.request.JobRequestRequest;
import com.kapotiabi.dto.request.ReviewRequest;
import com.kapotiabi.dto.request.ShopOfferRequest;
import com.kapotiabi.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class BookingIdorIT extends BaseIntegrationTest {

  private static final AtomicLong PLATE_COUNTER = new AtomicLong(2000);

  @Test
  void confirmBookingByNonShopOwner_returns403() throws Exception {
    Scenario s = scenario("conf");

    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/confirm")
                .header("Authorization", s.stranger))
        .andExpect(status().isForbidden());
  }

  @Test
  void scheduleBookingByNonShopOwner_returns403() throws Exception {
    Scenario s = scenario("sched");
    confirmAs(s.shopOwner, s.bookingId);

    LocalDateTime scheduledAt = LocalDateTime.now().plusDays(10).withNano(0);
    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/schedule")
                .header("Authorization", s.stranger)
                .param("scheduledAt", scheduledAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
        .andExpect(status().isForbidden());
  }

  @Test
  void completeBookingByNonShopOwner_returns403() throws Exception {
    Scenario s = scenario("comp");
    confirmAs(s.shopOwner, s.bookingId);

    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/complete")
                .header("Authorization", s.stranger))
        .andExpect(status().isForbidden());
  }

  @Test
  void cancelBookingByStranger_returns403() throws Exception {
    Scenario s = scenario("can");

    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/cancel").header("Authorization", s.stranger))
        .andExpect(status().isForbidden());
  }

  @Test
  void cancelBookingByCarOwner_succeeds() throws Exception {
    Scenario s = scenario("canc");

    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/cancel").header("Authorization", s.carOwner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("cancelled"));
  }

  @Test
  void cancelBookingByShopOwner_succeeds() throws Exception {
    Scenario s = scenario("cans");

    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/cancel")
                .header("Authorization", s.shopOwner))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("cancelled"));
  }

  @Test
  void submitReviewByNonBookingOwner_returns403() throws Exception {
    Scenario s = scenario("rev");
    confirmAs(s.shopOwner, s.bookingId);
    completeAs(s.shopOwner, s.bookingId);

    ReviewRequest req = new ReviewRequest(5, "tampered review");
    mockMvc
        .perform(
            post("/api/v1/bookings/" + s.bookingId + "/review")
                .header("Authorization", s.stranger)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getBookingById_byStranger_currentlyAllows_BUG() throws Exception {
    // TODO(security-bug): BookingController.getById has no ownership check.
    // Currently any authenticated user can read any booking. Should return 403 for
    // non-participants. Flip the assertion in this test to status().isForbidden() when
    // the bug is fixed.
    Scenario s = scenario("bug");

    mockMvc
        .perform(get("/api/v1/bookings/" + s.bookingId).header("Authorization", s.stranger))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              if (status == 403) {
                throw new AssertionError(
                    "Bug appears fixed (got 403). Flip this test's assertion to isForbidden().");
              }
            });
  }

  private record Scenario(String carOwner, String shopOwner, String stranger, long bookingId) {}

  private Scenario scenario(String prefix) throws Exception {
    String carOwner =
        registerAndGetToken(prefix + "-car@example.com", "Car" + prefix, UserRole.CAR_OWNER);
    String shopOwner =
        registerAndGetToken(prefix + "-shop@example.com", "Shop" + prefix, UserRole.SHOP_OWNER);
    String stranger =
        registerAndGetToken(
            prefix + "-stranger@example.com", "Stranger" + prefix, UserRole.CAR_OWNER);

    long carId = createCarFor(carOwner);
    long jobRequestId = createJobRequestFor(carOwner, carId);
    long offerId = createOfferFor(shopOwner, jobRequestId);
    long slotId = firstSlotIdOf(offerId, carOwner);
    long bookingId = acceptOffer(carOwner, offerId, slotId);

    return new Scenario(carOwner, shopOwner, stranger, bookingId);
  }

  private void confirmAs(String shopOwnerToken, long bookingId) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/bookings/" + bookingId + "/confirm")
                .header("Authorization", shopOwnerToken))
        .andExpect(status().isOk());
  }

  private void completeAs(String shopOwnerToken, long bookingId) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/bookings/" + bookingId + "/complete")
                .header("Authorization", shopOwnerToken))
        .andExpect(status().isOk());
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
    String plate = "BK" + PLATE_COUNTER.incrementAndGet();
    CarRequest car = new CarRequest(plate, "Toyota", "Corolla", 2018, "1.6L");
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
        new JobRequestRequest(carId, "Brake service", null, null, null, "Tallinn", "Harju", null);
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

  private long createOfferFor(String shopOwnerToken, long jobRequestId) throws Exception {
    LocalDateTime slot = LocalDateTime.now().plusDays(7).withNano(0);
    ShopOfferRequest req =
        new ShopOfferRequest(
            new BigDecimal("250.00"),
            "Brakes",
            "OEM",
            new BigDecimal("2.0"),
            LocalDateTime.now().plusDays(3),
            List.of(slot));
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/job-requests/" + jobRequestId + "/offers")
                    .header("Authorization", shopOwnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }

  private long firstSlotIdOf(long offerId, String token) throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/v1/offers/" + offerId).header("Authorization", token))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
    return body.get("slots").get(0).get("id").asLong();
  }

  private long acceptOffer(String carOwnerToken, long offerId, long slotId) throws Exception {
    AcceptOfferRequest req = new AcceptOfferRequest(slotId);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/offers/" + offerId + "/accept")
                    .header("Authorization", carOwnerToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }
}
