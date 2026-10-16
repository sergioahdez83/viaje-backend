package com.viaje.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.viaje.api.dto.DriverDTO;
import com.viaje.api.model.Driver;
import com.viaje.api.repository.DriverRepository;
import com.viaje.api.service.DriverService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DriverService driverService;

    private UUID driverId;
    private Driver mockDriver;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        driverId = UUID.randomUUID();

        mockDriver = new Driver();
        mockDriver.setId(driverId);
        mockDriver.setName("Sergio Hernandez");
        mockDriver.setLicenseNumber("LIC-9915-C");
        mockDriver.setActive(true);
    }

    @Test
    void createDriver() {
        when(driverRepository.save(any(Driver.class))).thenReturn(mockDriver);

        DriverDTO dtoInput = new DriverDTO(null, "Sergio Hernandez", "LIC-9915-C", true);
        DriverDTO result = driverService.createDriver(dtoInput);

        System.out.println("   -> Nombre del Chofer: " + result.getName());
        System.out.println("   -> Número de Licencia: " + result.getLicenseNumber());
        System.out.println("   -> Estatus Activo: " + result.getActive());
        
        assertNotNull(result);
        assertEquals("Sergio Hernandez", result.getName());
        assertEquals("LIC-9915-C", result.getLicenseNumber());
        verify(driverRepository, times(1)).save(any(Driver.class));
        
        System.out.println("========================================================\n");
    }

    @Test
    void getActiveDrivers() {
        List<Driver> activeDriversList = new ArrayList<>();
        activeDriversList.add(mockDriver);

        when(driverRepository.findAllActiveDrivers()).thenReturn(activeDriversList);

        List<DriverDTO> resultList = driverService.getActiveDrivers();
        
        System.out.println(" CONDUCTORES ACTIVOS  (" + resultList.size() + "):");
        for (DriverDTO dto : resultList) {
            System.out.println("   -> Chofer: " + dto.getName() + " | Licencia: " + dto.getLicenseNumber() + " | Activo: " + dto.getActive());
        }

        assertNotNull(resultList);
        assertFalse(resultList.isEmpty());
        assertEquals(1, resultList.size());
        assertEquals("Sergio Hernandez", resultList.get(0).getName());
        verify(driverRepository, times(1)).findAllActiveDrivers();
        
        System.out.println("========================================================\n");
    }
    
    @Test
    void getDriverById() {

        when(driverRepository.findById(driverId)).thenReturn(Optional.of(mockDriver));

        DriverDTO result = driverService.getDriverById(driverId);

        System.out.println(" RESPUESTA HTTP :");
        System.out.println("   -> ID Solicitado: " + result.getId());
        System.out.println("   -> Conductor Entregado: " + result.getName());

        assertNotNull(result);
        assertEquals(driverId, result.getId());
        verify(driverRepository, times(1)).findById(driverId);
        System.out.println("========================================================\n");
    }
    
    // prueba de conductor no existente
    @Test
    void getDriverById_Failed_DriverNotFound() {

        UUID idInvalido = UUID.randomUUID();
        // Simulamos que el repositorio devuelve vacío al buscar el ID falso
        when(driverRepository.findById(idInvalido)).thenReturn(Optional.empty());

        System.out.println(" Consultando ID inexistente por red: " + idInvalido);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            driverService.getDriverById(idInvalido);
        });

        // Imprimimos el error controlado por tu backend
        System.out.println(" EXCEPCION:");
        System.out.println("   -> Mensaje de Error: \"" + exception.getMessage() + "\"");

        verify(driverRepository, times(1)).findById(idInvalido);
        System.out.println("========================================================\n");
    }

}
