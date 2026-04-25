package com.kapotiabi.service;

import com.kapotiabi.dto.request.JobRequestRequest;
import com.kapotiabi.dto.response.JobRequestResponse;
import com.kapotiabi.entity.JobRequestEntity;
import com.kapotiabi.exception.ForbiddenException;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.repository.JobRequestRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JobRequestService {

  private final JobRequestRepository jobRequestRepository;

  public List<JobRequestResponse> getByUser(Long userId) {
    return jobRequestRepository.findByUserId(userId).stream()
        .map(JobRequestResponse::from)
        .toList();
  }

  public List<JobRequestResponse> getAvailable(String city, String county) {
    List<JobRequestEntity> results;
    if (city != null) {
      results = jobRequestRepository.findByStatusAndCity("pending", city);
    } else if (county != null) {
      results = jobRequestRepository.findByStatusAndCounty("pending", county);
    } else {
      results = jobRequestRepository.findByStatus("pending");
    }
    return results.stream().map(JobRequestResponse::from).toList();
  }

  public JobRequestResponse getById(Long id) {
    return JobRequestResponse.from(findOrThrow(id));
  }

  public JobRequestResponse create(JobRequestRequest req, Long authUserId) {
    LocalDateTime now = LocalDateTime.now();
    JobRequestEntity entity =
        JobRequestEntity.builder()
            .userId(authUserId)
            .carId(req.carId())
            .description(req.description())
            .acceptsNonOem(req.acceptsNonOem())
            .partsPreference(req.partsPreference())
            .searchScope(req.searchScope())
            .city(req.city())
            .county(req.county())
            .offerDeadline(req.offerDeadline())
            .status("pending")
            .createdAt(now)
            .updatedAt(now)
            .build();
    return JobRequestResponse.from(jobRequestRepository.save(entity));
  }

  public JobRequestResponse update(Long id, JobRequestRequest req, Long authUserId) {
    JobRequestEntity entity = findOrThrow(id);
    if (!authUserId.equals(entity.getUserId()))
      throw new ForbiddenException("You don't have access to this job request");
    entity.setCarId(req.carId());
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

  public JobRequestResponse cancel(Long id, Long authUserId) {
    JobRequestEntity entity = findOrThrow(id);
    if (!authUserId.equals(entity.getUserId()))
      throw new ForbiddenException("You don't have access to this job request");
    entity.setStatus("cancelled");
    entity.setUpdatedAt(LocalDateTime.now());
    return JobRequestResponse.from(jobRequestRepository.save(entity));
  }

  private JobRequestEntity findOrThrow(Long id) {
    return jobRequestRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Job request not found: " + id));
  }
}
