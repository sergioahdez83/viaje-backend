package com.viaje.api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.viaje.api.model.Driver;

import java.util.List;
import java.util.UUID;

@Repository
public interface  DriverRepository extends JpaRepository<Driver, UUID>{

    // lista todos los conductores activos
    @Query("SELECT d FROM Driver d WHERE d.active = true")
    List<Driver> findAllActiveDrivers();
}
