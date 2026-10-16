package com.viaje.api.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viaje.api.dto.DriverDTO;
import com.viaje.api.model.Driver;
import com.viaje.api.repository.DriverRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DriverService {

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Transactional
    public DriverDTO createDriver(DriverDTO dto) {
        log.info("Creando nuevo conductor con licencia: {}", dto.getLicenseNumber());

        Driver driver = new Driver();
        driver.setName(dto.getName());
        driver.setLicenseNumber(dto.getLicenseNumber());
        driver.setActive(dto.getActive() != null ? dto.getActive() : true);

        Driver savedDriver = driverRepository.save(driver);

        dto.setId(savedDriver.getId());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<DriverDTO> getActiveDrivers() {
        log.info("Consultando todos los conductores activos");
        return driverRepository.findAllActiveDrivers().stream()
                .map(d -> new DriverDTO(d.getId(), d.getName(), d.getLicenseNumber(), d.isActive()))
                .collect(Collectors.toList());
    }
    
 // Método para buscar un conductor individual y convertirlo a DTO
    @Transactional(readOnly = true)
    public DriverDTO getDriverById(UUID id) {
        Driver driver = driverRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conductor no encontrado con el ID: " + id));
        
        // Conversión manual (Manual Mapping) al DTO que espera el controlador
        return new DriverDTO(
                driver.getId(),
                driver.getName(),
                driver.getLicenseNumber(),
                driver.isActive()
        );
    }
}
