package com.velsis.speedviolationservice.repository;

import com.velsis.speedviolationservice.model.Violation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Repository
public class ViolationRepository {

    private final ConcurrentHashMap<String, List<Violation>> violationsByPlate = new ConcurrentHashMap<>();

    public void save(Violation violation) {
        violationsByPlate
            .computeIfAbsent(violation.licensePlate(), ignored -> new CopyOnWriteArrayList<>())
            .add(violation);
    }

    public List<Violation> findByLicensePlate(String licensePlate) {
        return violationsByPlate.getOrDefault(licensePlate, List.of());
    }
}
