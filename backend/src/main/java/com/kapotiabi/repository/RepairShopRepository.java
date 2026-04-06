package com.kapotiabi.repository;

import com.kapotiabi.entity.RepairShopEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RepairShopRepository extends JpaRepository<RepairShopEntity, Integer> {
    Optional<RepairShopEntity> findByOwnerId(Integer ownerId);
}
