package com.kapotiabi.repository;

import com.kapotiabi.entity.RepairShopEntity;
import com.kapotiabi.enums.RepairShopStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepairShopRepository extends JpaRepository<RepairShopEntity, Long> {
  Optional<RepairShopEntity> findByOwnerId(Long ownerId);

  List<RepairShopEntity> findByCity(String city);

  List<RepairShopEntity> findByCounty(String county);

  List<RepairShopEntity> findByStatus(RepairShopStatus status);

  Page<RepairShopEntity> findByStatus(RepairShopStatus status, Pageable pageable);

  List<RepairShopEntity> findByStatusAndCity(RepairShopStatus status, String city);

  Page<RepairShopEntity> findByStatusAndCity(
      RepairShopStatus status, String city, Pageable pageable);

  List<RepairShopEntity> findByStatusAndCounty(RepairShopStatus status, String county);

  Page<RepairShopEntity> findByStatusAndCounty(
      RepairShopStatus status, String county, Pageable pageable);
}
