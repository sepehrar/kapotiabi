package com.kapotiabi.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kapotiabi.dto.response.PagedResponse;
import com.kapotiabi.dto.response.SavedShopResponse;
import com.kapotiabi.entity.RepairShopEntity;
import com.kapotiabi.entity.UserSavedShopEntity;
import com.kapotiabi.exception.AlreadySavedException;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.repository.RepairShopRepository;
import com.kapotiabi.repository.UserSavedShopRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class UserSavedShopServiceTest {

  @Mock private UserSavedShopRepository savedShopRepository;
  @Mock private RepairShopRepository repairShopRepository;

  @InjectMocks private UserSavedShopService userSavedShopService;

  private RepairShopEntity buildShop() {
    return RepairShopEntity.builder()
        .id(10L)
        .ownerId(99L)
        .name("AutoExpert Tallinn")
        .city("Tallinn")
        .county("Harju")
        .phone("+372 555 1234")
        .status("active")
        .build();
  }

  private UserSavedShopEntity buildSaved() {
    return UserSavedShopEntity.builder()
        .id(1L)
        .userId(5L)
        .shopId(10L)
        .savedAt(LocalDateTime.now())
        .build();
  }

  // ── getSaved ──────────────────────────────────────────────────────────────

  @Test
  void getSaved_returnsPagedResult() {
    UserSavedShopEntity saved = buildSaved();
    RepairShopEntity shop = buildShop();
    when(savedShopRepository.findByUserId(any(Long.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(saved), PageRequest.of(0, 10), 1));
    when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));

    PagedResponse<SavedShopResponse> result = userSavedShopService.getSaved(5L, 0, 10);

    assertThat(result.content()).hasSize(1);
    assertThat(result.content().get(0).shopId()).isEqualTo(10L);
    assertThat(result.content().get(0).name()).isEqualTo("AutoExpert Tallinn");
    assertThat(result.totalElements()).isEqualTo(1);
    assertThat(result.totalPages()).isEqualTo(1);
    assertThat(result.page()).isEqualTo(0);
    assertThat(result.size()).isEqualTo(10);
  }

  @Test
  void getSaved_emptyList_returnsEmptyPage() {
    when(savedShopRepository.findByUserId(any(Long.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

    PagedResponse<SavedShopResponse> result = userSavedShopService.getSaved(5L, 0, 10);

    assertThat(result.content()).isEmpty();
    assertThat(result.totalElements()).isEqualTo(0);
  }

  @Test
  void getSaved_shopMissing_throwsNotFoundException() {
    UserSavedShopEntity saved = buildSaved();
    when(savedShopRepository.findByUserId(any(Long.class), any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of(saved), PageRequest.of(0, 10), 1));
    when(repairShopRepository.findById(10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userSavedShopService.getSaved(5L, 0, 10))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("10");
  }

  // ── save ──────────────────────────────────────────────────────────────────

  @Test
  void save_success_returnsSavedShopResponse() {
    RepairShopEntity shop = buildShop();
    UserSavedShopEntity saved = buildSaved();
    when(repairShopRepository.findById(10L)).thenReturn(Optional.of(shop));
    when(savedShopRepository.existsByUserIdAndShopId(5L, 10L)).thenReturn(false);
    when(savedShopRepository.save(any(UserSavedShopEntity.class))).thenReturn(saved);

    SavedShopResponse response = userSavedShopService.save(5L, 10L);

    assertThat(response.shopId()).isEqualTo(10L);
    assertThat(response.name()).isEqualTo("AutoExpert Tallinn");
    verify(savedShopRepository).save(any(UserSavedShopEntity.class));
  }

  @Test
  void save_shopNotFound_throwsNotFoundException() {
    when(repairShopRepository.findById(99L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userSavedShopService.save(5L, 99L))
        .isInstanceOf(NotFoundException.class)
        .hasMessageContaining("99");
  }

  @Test
  void save_alreadySaved_throwsAlreadySavedException() {
    when(repairShopRepository.findById(10L)).thenReturn(Optional.of(buildShop()));
    when(savedShopRepository.existsByUserIdAndShopId(5L, 10L)).thenReturn(true);

    assertThatThrownBy(() -> userSavedShopService.save(5L, 10L))
        .isInstanceOf(AlreadySavedException.class)
        .hasMessage("Shop already saved");
  }

  // ── unsave ────────────────────────────────────────────────────────────────

  @Test
  void unsave_success_deletesRecord() {
    when(savedShopRepository.findByUserIdAndShopId(5L, 10L)).thenReturn(Optional.of(buildSaved()));

    userSavedShopService.unsave(5L, 10L);

    verify(savedShopRepository).deleteByUserIdAndShopId(5L, 10L);
  }

  @Test
  void unsave_notFound_throwsNotFoundException() {
    when(savedShopRepository.findByUserIdAndShopId(5L, 10L)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> userSavedShopService.unsave(5L, 10L))
        .isInstanceOf(NotFoundException.class);
  }
}
