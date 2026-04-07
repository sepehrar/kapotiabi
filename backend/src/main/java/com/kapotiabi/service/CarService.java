package com.kapotiabi.service;

import com.kapotiabi.dto.request.CarRequest;
import com.kapotiabi.dto.response.CarResponse;
import com.kapotiabi.entity.CarEntity;
import com.kapotiabi.exception.NotFoundException;
import com.kapotiabi.repository.CarRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CarService {

  private final CarRepository carRepository;

  public List<CarResponse> getByOwner(Long ownerId) {
    return carRepository.findByOwnerId(ownerId).stream().map(CarResponse::from).toList();
  }

  public CarResponse getById(Long id) {
    return CarResponse.from(findOrThrow(id));
  }

  public CarResponse create(CarRequest req) {
    CarEntity car =
        CarEntity.builder()
            .ownerId(req.ownerId())
            .plateNumber(req.plateNumber())
            .make(req.make())
            .model(req.model())
            .year(req.year())
            .engine(req.engine())
            .createdAt(LocalDateTime.now())
            .build();
    return CarResponse.from(carRepository.save(car));
  }

  public CarResponse update(Long id, CarRequest req) {
    CarEntity car = findOrThrow(id);
    car.setOwnerId(req.ownerId());
    car.setPlateNumber(req.plateNumber());
    car.setMake(req.make());
    car.setModel(req.model());
    car.setYear(req.year());
    car.setEngine(req.engine());
    return CarResponse.from(carRepository.save(car));
  }

  public void delete(Long id) {
    findOrThrow(id);
    carRepository.deleteById(id);
  }

  private CarEntity findOrThrow(Long id) {
    return carRepository
        .findById(id)
        .orElseThrow(() -> new NotFoundException("Car not found: " + id));
  }
}
