package com.kapotiabi.service;

import com.kapotiabi.dto.request.JobRequestRequest;
import com.kapotiabi.dto.response.JobRequestResponse;
import com.kapotiabi.entity.JobRequestEntity;
import com.kapotiabi.enums.JobRequestStatus;
import com.kapotiabi.exception.ForbiddenException;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.repository.CarRepository;
import com.kapotiabi.repository.JobRequestRepository;
import com.kapotiabi.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JobRequestService {

  private final JobRequestRepository jobRequestRepository;
  private final UserRepository userRepository;
  private final CarRepository carRepository;

  public List<JobRequestResponse> getByUser(Long userId) {
    return jobRequestRepository.findByUserId(userId).stream()
        .map(JobRequestResponse::from)
        .toList();
  }

  public Page<JobRequestResponse> getAvailable(String city, String county, Pageable pageable) {
    if (city != null) {
      return jobRequestRepository
          .findByStatusAndCity(JobRequestStatus.PENDING, city, pageable)
          .map(JobRequestResponse::from);
    } else if (county != null) {
      return jobRequestRepository
          .findByStatusAndCounty(JobRequestStatus.PENDING, county, pageable)
          .map(JobRequestResponse::from);
    } else {
      return jobRequestRepository
          .findByStatus(JobRequestStatus.PENDING, pageable)
          .map(JobRequestResponse::from);
    }
  }

  public JobRequestResponse getById(Long id) {
    return JobRequestResponse.from(findOrThrow(id));
  }

  @Transactional
  public JobRequestResponse create(JobRequestRequest req, Long authUserId) {
    LocalDateTime now = LocalDateTime.now();
    JobRequestEntity entity =
        JobRequestEntity.builder()
            .user(userRepository.getReferenceById(authUserId))
            .car(carRepository.getReferenceById(req.carId()))
            .description(req.description())
            .acceptsNonOem(req.acceptsNonOem())
            .partsPreference(req.partsPreference())
            .searchScope(req.searchScope())
            .city(req.city())
            .county(req.county())
            .offerDeadline(req.offerDeadline())
            .status(JobRequestStatus.PENDING)
            .createdAt(now)
            .updatedAt(now)
            .build();
    return JobRequestResponse.from(jobRequestRepository.save(entity));
  }

  @Transactional
  public JobRequestResponse update(Long id, JobRequestRequest req, Long authUserId) {
    JobRequestEntity entity = findOrThrow(id);
    if (!authUserId.equals(entity.getUser().getId()))
      throw new ForbiddenException("You don't have access to this job request");
    entity.setCar(carRepository.getReferenceById(req.carId()));
    entity.setDescription(req.description());
    entity.setAcceptsNonOem(req.acceptsNonOem());
    entity.setPartsPreference(req.partsPreference());
    entity.setSearchScope(req.searchScope());
    entity.setCity(req.city());
    entity.setCounty(req.county());
    entity.setOfferDeadline(req.offerDeadline());
    entity.setUpdatedAt(LocalDateTime.now());
    return JobRequestResponse.from(jobRequestRepository.save(entity));
  }

  @Transactional
  public JobRequestResponse cancel(Long id, Long authUserId) {
    JobRequestEntity entity = findOrThrow(id);
    if (!authUserId.equals(entity.getUser().getId()))
      throw new ForbiddenException("You don't have access to this job request");
    entity.getStatus().assertCanTransitionTo(JobRequestStatus.CANCELLED);
    entity.setStatus(JobRequestStatus.CANCELLED);
    entity.setUpdatedAt(LocalDateTime.now());
    return JobRequestResponse.from(jobRequestRepository.save(entity));
  }

  private JobRequestEntity findOrThrow(Long id) {
    return jobRequestRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Job request not found: " + id));
  }
}
