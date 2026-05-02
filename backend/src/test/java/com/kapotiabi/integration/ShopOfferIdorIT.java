package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.dto.request.AcceptOfferRequest;
import com.kapotiabi.dto.request.CarRequest;
import com.kapotiabi.dto.request.JobRequestRequest;
import com.kapotiabi.dto.request.ShopOfferRequest;
import com.kapotiabi.enums.UserRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class ShopOfferIdorIT extends BaseIntegrationTest {

  private static final AtomicLong PLATE_COUNTER = new AtomicLong(1000);

  @Test
  void updateOfferByDifferentShopOwner_returns403() throws Exception {
    String shopOwnerA =
        registerAndGetToken("offer-shop-a@example.com", "Anna", UserRole.SHOP_OWNER);
    String shopOwnerB =
        registerAndGetToken("offer-shop-b@example.com", "Bert", UserRole.SHOP_OWNER);
    String carOwnerC = registerAndGetToken("offer-car-c@example.com", "Cara", UserRole.CAR_OWNER);

    long carId = createCarFor(carOwnerC);
    long jobRequestId = createJobRequestFor(carOwnerC, carId);
    long offerId = createOfferFor(shopOwnerA, jobRequestId);

    ShopOfferRequest req =
        new ShopOfferRequest(
            new BigDecimal("999.00"), "tampered", "non_oem", BigDecimal.ONE, null, List.of());

    mockMvc
        .perform(
            put("/api/v1/offers/" + offerId)
                .header("Authorization", shopOwnerB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden());
  }

  @Test
  void uploadOfferPdfByDifferentShopOwner_returns403() throws Exception {
    String shopOwnerA =
        registerAndGetToken("offer-pdfshop-a@example.com", "Anya", UserRole.SHOP_OWNER);
    String shopOwnerB =
        registerAndGetToken("offer-pdfshop-b@example.com", "Bree", UserRole.SHOP_OWNER);
    String carOwnerC =
        registerAndGetToken("offer-pdfcar-c@example.com", "Clio", UserRole.CAR_OWNER);

    long carId = createCarFor(carOwnerC);
    long jobRequestId = createJobRequestFor(carOwnerC, carId);
    long offerId = createOfferFor(shopOwnerA, jobRequestId);

    MockMultipartFile pdf =
        new MockMultipartFile("file", "offer.pdf", "application/pdf", "%PDF-test".getBytes());

    mockMvc
        .perform(
            multipart("/api/v1/offers/" + offerId + "/pdf")
                .file(pdf)
                .header("Authorization", shopOwnerB))
        .andExpect(status().isForbidden());
  }

  @Test
  void rejectOfferByNonCarOwner_returns403() throws Exception {
    String shopOwnerA =
        registerAndGetToken("offer-rej-shop@example.com", "Aaron", UserRole.SHOP_OWNER);
    String carOwnerC =
        registerAndGetToken("offer-rej-car@example.com", "Camille", UserRole.CAR_OWNER);
    String carOwnerD =
        registerAndGetToken("offer-rej-stranger@example.com", "Dora", UserRole.CAR_OWNER);

    long carId = createCarFor(carOwnerC);
    long jobRequestId = createJobRequestFor(carOwnerC, carId);
    long offerId = createOfferFor(shopOwnerA, jobRequestId);

    mockMvc
        .perform(post("/api/v1/offers/" + offerId + "/reject").header("Authorization", carOwnerD))
        .andExpect(status().isForbidden());
  }

  @Test
  void acceptOfferByNonCarOwner_returns403() throws Exception {
    String shopOwnerA =
        registerAndGetToken("offer-acc-shop@example.com", "Aleks", UserRole.SHOP_OWNER);
    String carOwnerC =
        registerAndGetToken("offer-acc-car@example.com", "Cathy", UserRole.CAR_OWNER);
    String carOwnerD =
        registerAndGetToken("offer-acc-stranger@example.com", "Dom", UserRole.CAR_OWNER);

    long carId = createCarFor(carOwnerC);
    long jobRequestId = createJobRequestFor(carOwnerC, carId);
    long offerId = createOfferFor(shopOwnerA, jobRequestId);
    long slotId = firstSlotIdOf(offerId, carOwnerC);

    AcceptOfferRequest req = new AcceptOfferRequest(slotId);

    mockMvc
        .perform(
            post("/api/v1/offers/" + offerId + "/accept")
                .header("Authorization", carOwnerD)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden());
  }

  @Test
  void downloadOfferPdf_byStranger_currentlyAllows_BUG() throws Exception {
    // TODO(security-bug): ShopOfferService.downloadPdf has no ownership check.
    // Currently any authenticated user can download any offer's PDF. Should return 403
    // for non-participants. Flip the assertion in this test to status().isForbidden()
    // when the bug is fixed.
    String shopOwnerA =
        registerAndGetToken("offer-dl-shop@example.com", "Aiden", UserRole.SHOP_OWNER);
    String carOwnerC = registerAndGetToken("offer-dl-car@example.com", "Celia", UserRole.CAR_OWNER);
    String stranger =
        registerAndGetToken("offer-dl-stranger@example.com", "Stan", UserRole.CAR_OWNER);

    long carId = createCarFor(carOwnerC);
    long jobRequestId = createJobRequestFor(carOwnerC, carId);
    long offerId = createOfferFor(shopOwnerA, jobRequestId);

    MockMultipartFile pdf =
        new MockMultipartFile("file", "offer.pdf", "application/pdf", "%PDF-1.4 test".getBytes());
    mockMvc
        .perform(
            multipart("/api/v1/offers/" + offerId + "/pdf")
                .file(pdf)
                .header("Authorization", shopOwnerA))
        .andExpect(status().isOk());

    mockMvc
        .perform(get("/api/v1/offers/" + offerId + "/pdf").header("Authorization", stranger))
        .andExpect(
            result -> {
              int status = result.getResponse().getStatus();
              if (status == 403) {
                throw new AssertionError(
                    "Bug appears fixed (got 403). Flip this test's assertion to isForbidden().");
              }
            });
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
    String plate = "OF" + PLATE_COUNTER.incrementAndGet();
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

  private long createOfferFor(String shopOwnerToken, long jobRequestId) throws Exception {
    LocalDateTime slot = LocalDateTime.now().plusDays(7).withNano(0);
    ShopOfferRequest req =
        new ShopOfferRequest(
            new BigDecimal("250.00"),
            "Brake pads + labor",
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
}
