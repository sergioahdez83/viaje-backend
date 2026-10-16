package com.viaje.api.dto;

import com.viaje.api.model.OrderStatus;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
	
	private UUID id;

    private OrderStatus status;

    @NotBlank(message = "El origen es obligatorio")
    private String origin;

    @NotBlank(message = "El destino es obligatorio")
    private String destination;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Al transferir datos con el cliente, solo enviamos el ID del chofer asignado
    private UUID driverId;

}
