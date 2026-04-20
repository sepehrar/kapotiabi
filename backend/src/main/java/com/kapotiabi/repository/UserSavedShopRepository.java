package com.kapotiabi.repository;

import com.kapotiabi.entity.UserSavedShopEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSavedShopRepository extends JpaRepository<UserSavedShopEntity, Long> {

  List<UserSavedShopEntity> findByUserId(Long userId);

  Page<UserSavedShopEntity> findByUserId(Long userId, Pageable pageable);

  Optional<UserSavedShopEntity> findByUserIdAndShopId(Long userId, Long shopId);

  boolean existsByUserIdAndShopId(Long userId, Long shopId);

  void deleteByUserIdAndShopId(Long userId, Long shopId);
}
