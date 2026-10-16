package com.viaje.api.client;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.viaje.api.dto.DriverDTO;

// se le indica el nombre del contenedor de destino en Docker y su puerto
@Component
@FeignClient(name = "driver-service", url = "http://viaje-driver-service:8081/api/drivers")
public interface DriverClient {

    // firma exacta del método GET que consumirá al driver-service por red
    @GetMapping("/{id}")
    DriverDTO getDriverById(@PathVariable("id") UUID id);
}
