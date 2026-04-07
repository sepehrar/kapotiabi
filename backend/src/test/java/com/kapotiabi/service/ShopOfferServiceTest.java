package com.kapotiabi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kapotiabi.dto.request.ShopOfferRequest;
import com.kapotiabi.dto.response.ShopOfferResponse;
import com.kapotiabi.entity.JobRequestEntity;
import com.kapotiabi.entity.ShopOfferEntity;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.repository.JobRequestRepository;
import com.kapotiabi.repository.ShopOfferRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShopOfferServiceTest {

  @Mock private ShopOfferRepository shopOfferRepository;

  @Mock private JobRequestRepository jobRequestRepository;

  @InjectMocks private ShopOfferService shopOfferService;

  private ShopOfferEntity buildOffer(String status) {
    return ShopOfferEntity.builder()
        .id(1L)
        .jobRequestId(10L)
        .shopId(2L)
        .priceEstimate(new BigDecimal("100.00"))
        .status(status)
        .createdAt(LocalDateTime.now())
        .build();
  }

  private JobRequestEntity buildJobRequest(String status) {
    return JobRequestEntity.builder()
        .id(10L)
        .userId(5L)
        .carId(3L)
        .description("Oil change")
        .status(status)
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  @Test
  void getByJobRequest() {
    when(shopOfferRepository.findByJobRequestId(10L)).thenReturn(List.of(buildOffer("pending")));

    List<ShopOfferResponse> result = shopOfferService.getByJobRequest(10L);

    assertThat(result).hasSize(1);
    assertThat(result.get(0).jobRequestId()).isEqualTo(10L);
  }

  @Test
  void getById() {
    when(shopOfferRepository.findById(1L)).thenReturn(Optional.of(buildOffer("pending")));

    ShopOfferResponse response = shopOfferService.getById(1L);

    assertThat(response.id()).isEqualTo(1L);
  }

  @Test
  void create_updatesPending() {
    JobRequestEntity jobRequest = buildJobRequest("pending");
    when(jobRequestRepository.findById(10L)).thenReturn(Optional.of(jobRequest));
    ShopOfferEntity savedOffer = buildOffer("pending");
    when(shopOfferRepository.save(any(ShopOfferEntity.class))).thenReturn(savedOffer);
    when(jobRequestRepository.save(any(JobRequestEntity.class))).thenReturn(jobRequest);

    ShopOfferRequest req =
        new ShopOfferRequest(
            2L, new BigDecimal("100.00"), "breakdown", "OEM", new BigDecimal("2.0"), null);
    ShopOfferResponse response = shopOfferService.create(10L, req);

    assertThat(response.shopId()).isEqualTo(2L);
    verify(jobRequestRepository).save(any(JobRequestEntity.class));
  }

  @Test
  void create_alreadyOffers() {
    JobRequestEntity jobRequest = buildJobRequest("offers_received");
    when(jobRequestRepository.findById(10L)).thenReturn(Optional.of(jobRequest));
    ShopOfferEntity savedOffer = buildOffer("pending");
    when(shopOfferRepository.save(any(ShopOfferEntity.class))).thenReturn(savedOffer);

    ShopOfferRequest req =
        new ShopOfferRequest(2L, new BigDecimal("100.00"), null, null, null, null);
    ShopOfferResponse response = shopOfferService.create(10L, req);

    // Status is already offers_received, no update needed
    assertThat(response).isNotNull();
  }

  @Test
  void reject() {
    ShopOfferEntity offer = buildOffer("pending");
    when(shopOfferRepository.findById(1L)).thenReturn(Optional.of(offer));
    when(shopOfferRepository.save(any(ShopOfferEntity.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    ShopOfferResponse response = shopOfferService.reject(1L);

    assertThat(response.status()).isEqualTo("rejected");
  }

  @Test
  void getById_notFound() {
    when(shopOfferRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> shopOfferService.getById(99L)).isInstanceOf(NotFoundException.class);
  }
}
