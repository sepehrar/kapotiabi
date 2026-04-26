package com.kapotiabi.repository;

import com.kapotiabi.entity.JobRequestEntity;
import com.kapotiabi.enums.JobRequestStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRequestRepository extends JpaRepository<JobRequestEntity, Long> {
  List<JobRequestEntity> findByUserId(Long userId);

  List<JobRequestEntity> findByStatus(JobRequestStatus status);

  Page<JobRequestEntity> findByStatus(JobRequestStatus status, Pageable pageable);

  List<JobRequestEntity> findByStatusAndCity(JobRequestStatus status, String city);

  Page<JobRequestEntity> findByStatusAndCity(
      JobRequestStatus status, String city, Pageable pageable);

  List<JobRequestEntity> findByStatusAndCounty(JobRequestStatus status, String county);

  Page<JobRequestEntity> findByStatusAndCounty(
      JobRequestStatus status, String county, Pageable pageable);
}
