package com.kapotiabi.controller;

import com.kapotiabi.dto.request.GuestEstimateRequest;
import com.kapotiabi.dto.response.GuestEstimateResponse;
import com.kapotiabi.service.GuestEstimateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/v1/guest-estimates")
@RequiredArgsConstructor
@Tag(
    name = "Guest Estimates",
    description = "Guest price estimate management (no account required)")
public class GuestEstimateController {

  private final GuestEstimateService guestEstimateService;

  @PostMapping
  @Operation(summary = "Create a guest price estimate")
  public ResponseEntity<GuestEstimateResponse> create(
      @Valid @RequestBody GuestEstimateRequest req) {
    GuestEstimateResponse created = guestEstimateService.create(req);
    URI location =
        ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(created.id())
            .toUri();
    return ResponseEntity.created(location).body(created);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get a guest estimate by ID")
  public ResponseEntity<GuestEstimateResponse> getById(@PathVariable Long id) {
    return ResponseEntity.ok(guestEstimateService.getById(id));
  }
}
