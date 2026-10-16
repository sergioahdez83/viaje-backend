package com.viaje.api.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.viaje.api.client.DriverClient;
import com.viaje.api.dto.OrderDTO;
import com.viaje.api.model.Order;
import com.viaje.api.model.OrderStatus;
import com.viaje.api.repository.OrderRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderService {
	
	private  OrderRepository orderRepository;
    private  DriverClient driverClient;
    
    // Inyección por constructor (Buenas prácticas SOLID)
	public OrderService(OrderRepository orderRepository, DriverClient driverClient) {
		super();
		this.orderRepository = orderRepository;
		this.driverClient = driverClient;
	}
    
	@Transactional
    public OrderDTO createOrder(OrderDTO dto) {
        log.info("Creando nueva orden con origen: {} y destino: {}", dto.getOrigin(), dto.getDestination());
        
        Order order = new Order();
        order.setStatus(OrderStatus.CREATED);
        order.setOrigin(dto.getOrigin());
        order.setDestination(dto.getDestination());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        Order savedOrder = orderRepository.save(order);
        
        dto.setId(savedOrder.getId());
        dto.setStatus(savedOrder.getStatus());
        dto.setCreatedAt(savedOrder.getCreatedAt());
        dto.setUpdatedAt(savedOrder.getUpdatedAt());
        return dto;
    }
	
	@Transactional
    public OrderDTO changeStatus(UUID orderId, OrderStatus newStatus) {
        log.info("Cambiando estado de la orden {} a {}", orderId, newStatus);
        
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new RuntimeException("Orden no encontrada"));

        // Validar flujo lógico entre estados
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        order.setUpdatedAt(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(order);

        return mapToDTO(updatedOrder);
    }
	
	@Transactional
    public OrderDTO assignDriverWithFiles(UUID orderId, UUID driverId, byte[] pdfBytes, byte[] imgBytes, String imageContentType, String pdfContentType) {
        log.info("Asignando conductor {} a la orden {}", driverId, orderId);
        
        // valida que solo sea .png o .jpg, jpg se considera como jpg
        if (imageContentType == null || 
            (!imageContentType.equalsIgnoreCase("image/png") && 
             !imageContentType.equalsIgnoreCase("image/jpeg") && 
             !imageContentType.equalsIgnoreCase("image/jpg"))) {
            throw new RuntimeException("Formato de imagen no permitido. Solo se aceptan archivos .png y .jpg");
        }
        
        // valida pdf 
        if (pdfContentType == null || !pdfContentType.equalsIgnoreCase("application/pdf")) {
            throw new RuntimeException("Formato de documento no permitido. Solo se aceptan archivos .pdf");
        }

        Order order = orderRepository.findById(orderId)  .orElseThrow(() -> new RuntimeException("Orden no encontrada"));
 
        com.viaje.api.dto.DriverDTO driver = driverClient.getDriverById(driverId);
        if (driver == null) {
            throw new RuntimeException("El conductor no existe en el sistema distribuido");
        }

        if (!driver.getActive()) {
            throw new RuntimeException("El conductor no se encuentra activo para asignación");
        }


        if (order.getStatus() != OrderStatus.CREATED) {
            throw new RuntimeException("La orden debe estar en estado CREATED para poder asignarse");
        }

        order.setDriverId(driverId);
        order.setStatus(OrderStatus.IN_TRANSIT); // al asignar pasa automaticamente a transito
        order.setAsignarPdf(pdfBytes);
        order.setAsignarImage(imgBytes);
        order.setUpdatedAt(LocalDateTime.now());

        Order updatedOrder = orderRepository.save(order);
        return mapToDTO(updatedOrder);
    }
	
	private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Reglas de negocio para evitar flujos imposibles
        if (current == OrderStatus.DELIVERED || current == OrderStatus.CANCELLED) {
            throw new RuntimeException("No se puede modificar el estado de una orden finalizada o cancelada");
        }
        if (current == OrderStatus.CREATED && next == OrderStatus.DELIVERED) {
            throw new RuntimeException("No se puede pasar una orden de CREATED directo a DELIVERED sin transitar");
        }
    }

	private OrderDTO mapToDTO(Order order) {
	    return new OrderDTO(
	            order.getId(),
	            order.getStatus(),
	            order.getOrigin(),
	            order.getDestination(),
	            order.getCreatedAt(),
	            order.getUpdatedAt(),
	            order.getDriverId() // Mapeo directo del UUID 
	    );
	}

    @Transactional(readOnly = true)
    public java.util.List<OrderDTO> getOrdersWithFilters(
            OrderStatus status, 
            String origin, 
            String destination, 
            java.time.LocalDate date) {
        
        log.info("Buscando ordenes con filtros dinamicos avanzados");
        
        org.springframework.data.jpa.domain.Specification<Order> spec = (root, query, cb) -> {
            java.util.List<javax.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (origin != null && !origin.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("origin")), "%" + origin.toLowerCase() + "%"));
            }
            if (destination != null && !destination.trim().isEmpty()) {
                predicates.add(cb.like(cb.lower(root.get("destination")), "%" + destination.toLowerCase() + "%"));
            }
            if (date != null) {
                // Filtra las ordenes creadas durante todo ese dia (desde las 00:00 hasta las 23:59)
                java.time.LocalDateTime startOfDay = date.atStartOfDay();
                java.time.LocalDateTime endOfDay = date.atTime(23, 59, 59);
                predicates.add(cb.between(root.get("createdAt"), startOfDay, endOfDay));
            }
            
            return cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };

        return orderRepository.findAll(spec).stream()
                .map(this::mapToDTO)
                .collect(java.util.stream.Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(UUID id) {
        log.info("Buscando orden en la base de datos por ID: {}", id);
        Order order = orderRepository.findById(id).orElseThrow(() -> new RuntimeException("Orden no encontrada con el ID proporcionado"));
        return mapToDTO(order);
    }

}
