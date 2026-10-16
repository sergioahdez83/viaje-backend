package com.viaje.api.controller;

import com.viaje.api.dto.DriverDTO;
import com.viaje.api.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/drivers")
public class DriverController {
	
	private final DriverService driverService;
	
	// se cra el constructor
    public DriverController(DriverService driverService) {
        this.driverService = driverService;
    }
    
    // endpoint para CREAR un conductor (Aquí se usa @Valid en la entrada)
    @PostMapping
    public ResponseEntity<DriverDTO> createDriver(@Valid @RequestBody DriverDTO dto) {
        DriverDTO created = driverService.createDriver(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    // endpoint para LISTAR todos los conductores activos
    @GetMapping("/active")
    public ResponseEntity<List<DriverDTO>> getActiveDrivers() {
        List<DriverDTO> activeDrivers = driverService.getActiveDrivers();
        return ResponseEntity.ok(activeDrivers);
    }
    
    // endpoint para BUSCAr conductor por ID
    @GetMapping("/{id}")
    public ResponseEntity<DriverDTO> getDriverById(@PathVariable UUID id) {
        DriverDTO driverDTO = driverService.getDriverById(id);
        return ResponseEntity.ok(driverDTO);
    }

}
