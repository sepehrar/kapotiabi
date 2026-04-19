package com.kapotiabi.service;

import com.kapotiabi.dto.response.PagedResponse;
import com.kapotiabi.dto.response.SavedShopResponse;
import com.kapotiabi.entity.RepairShopEntity;
import com.kapotiabi.entity.UserSavedShopEntity;
import com.kapotiabi.exception.AlreadySavedException;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.repository.RepairShopRepository;
import com.kapotiabi.repository.UserSavedShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSavedShopService {

  private final UserSavedShopRepository savedShopRepository;
  private final RepairShopRepository repairShopRepository;

  public PagedResponse<SavedShopResponse> getSaved(Long userId, int page, int size) {
    var pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());
    return PagedResponse.from(
        savedShopRepository
            .findByUserId(userId, pageable)
            .map(
                saved -> {
                  RepairShopEntity shop =
                      repairShopRepository
                          .findById(saved.getShopId())
                          .orElseThrow(
                              () -> new NotFoundException("Shop not found: " + saved.getShopId()));
                  return SavedShopResponse.from(saved, shop);
                }));
  }

  public SavedShopResponse save(Long userId, Long shopId) {
    RepairShopEntity shop =
        repairShopRepository
            .findById(shopId)
            .orElseThrow(() -> new NotFoundException("Shop not found: " + shopId));
    if (savedShopRepository.existsByUserIdAndShopId(userId, shopId)) {
      throw new AlreadySavedException();
    }
    UserSavedShopEntity saved =
        savedShopRepository.save(
            UserSavedShopEntity.builder().userId(userId).shopId(shopId).build());
    return SavedShopResponse.from(saved, shop);
  }

  @Transactional
  public void unsave(Long userId, Long shopId) {
    savedShopRepository
        .findByUserIdAndShopId(userId, shopId)
        .orElseThrow(() -> new NotFoundException("Saved shop not found"));
    savedShopRepository.deleteByUserIdAndShopId(userId, shopId);
  }
}
