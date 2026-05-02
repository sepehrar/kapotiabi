package com.kapotiabi.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.kapotiabi.dto.RegisterRequest;
import com.kapotiabi.dto.request.RepairShopRequest;
import com.kapotiabi.dto.request.ShopServiceCategoryRequest;
import com.kapotiabi.enums.UserRole;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

class RepairShopIdorIT extends BaseIntegrationTest {

  @Test
  void updateShopByNonOwner_returns403() throws Exception {
    String shopOwnerA = registerAndGetToken("shop-upd-a@example.com", "Anna", UserRole.SHOP_OWNER);
    String shopOwnerB = registerAndGetToken("shop-upd-b@example.com", "Bert", UserRole.SHOP_OWNER);

    long shopId = createShopFor(shopOwnerA, "Anna's Garage");

    RepairShopRequest req =
        new RepairShopRequest("Hijacked", null, null, null, null, null, null, null);

    mockMvc
        .perform(
            put("/api/v1/shops/" + shopId)
                .header("Authorization", shopOwnerB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden());
  }

  @Test
  void addCategoryToOtherShop_returns403() throws Exception {
    String shopOwnerA = registerAndGetToken("shop-cat-a@example.com", "Anya", UserRole.SHOP_OWNER);
    String shopOwnerB = registerAndGetToken("shop-cat-b@example.com", "Bree", UserRole.SHOP_OWNER);

    long shopAId = createShopFor(shopOwnerA, "Anya's Garage");

    ShopServiceCategoryRequest req =
        new ShopServiceCategoryRequest(
            "oil_change", new BigDecimal("30.00"), new BigDecimal("60.00"), "OEM");

    mockMvc
        .perform(
            post("/api/v1/shops/" + shopAId + "/categories")
                .header("Authorization", shopOwnerB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isForbidden());
  }

  @Test
  void updateCategoryOnOtherShop_returns403() throws Exception {
    String shopOwnerA = registerAndGetToken("shop-cu-a@example.com", "Aleks", UserRole.SHOP_OWNER);
    String shopOwnerB = registerAndGetToken("shop-cu-b@example.com", "Bram", UserRole.SHOP_OWNER);

    long shopAId = createShopFor(shopOwnerA, "Aleks Garage");
    long catId =
        addCategoryFor(
            shopOwnerA,
            shopAId,
            new ShopServiceCategoryRequest(
                "tires", new BigDecimal("50.00"), new BigDecimal("100.00"), "OEM"));

    ShopServiceCategoryRequest tampered =
        new ShopServiceCategoryRequest(
            "tampered", new BigDecimal("1.00"), new BigDecimal("2.00"), "non_oem");

    mockMvc
        .perform(
            put("/api/v1/shops/" + shopAId + "/categories/" + catId)
                .header("Authorization", shopOwnerB)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(tampered)))
        .andExpect(status().isForbidden());
  }

  @Test
  void deleteCategoryOnOtherShop_returns403() throws Exception {
    String shopOwnerA = registerAndGetToken("shop-cd-a@example.com", "Alex", UserRole.SHOP_OWNER);
    String shopOwnerB = registerAndGetToken("shop-cd-b@example.com", "Boyd", UserRole.SHOP_OWNER);

    long shopAId = createShopFor(shopOwnerA, "Alex Garage");
    long catId =
        addCategoryFor(
            shopOwnerA,
            shopAId,
            new ShopServiceCategoryRequest(
                "brakes", new BigDecimal("80.00"), new BigDecimal("200.00"), "OEM"));

    mockMvc
        .perform(
            delete("/api/v1/shops/" + shopAId + "/categories/" + catId)
                .header("Authorization", shopOwnerB))
        .andExpect(status().isForbidden());
  }

  @Test
  void addCategoryToOwnShop_succeeds() throws Exception {
    String shopOwnerA = registerAndGetToken("shop-own-a@example.com", "Asta", UserRole.SHOP_OWNER);

    long shopAId = createShopFor(shopOwnerA, "Asta Garage");

    ShopServiceCategoryRequest req =
        new ShopServiceCategoryRequest(
            "oil_change", new BigDecimal("30.00"), new BigDecimal("60.00"), "OEM");

    mockMvc
        .perform(
            post("/api/v1/shops/" + shopAId + "/categories")
                .header("Authorization", shopOwnerA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
        .andExpect(status().isCreated());
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

  private long createShopFor(String token, String name) throws Exception {
    RepairShopRequest req =
        new RepairShopRequest(name, "Tartu mnt 1", "Tallinn", "Harju", null, null, null, null);
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/shops")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }

  private long addCategoryFor(String token, long shopId, ShopServiceCategoryRequest req)
      throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/shops/" + shopId + "/categories")
                    .header("Authorization", token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn();
    return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
  }
}
