package com.viaje.api.controller;

import com.viaje.api.dto.OrderDTO;
import com.viaje.api.model.OrderStatus;
import com.viaje.api.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.UUID;
@RestController
@RequestMapping("/api/orders")
public class OrderController {
	
	private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // endpoint para CREAR una orden (Aquí usamos @Valid en el DTO)
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderDTO dto) {
        OrderDTO created = orderService.createOrder(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    // endpoint para CAMBIAR el estado de una orden
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> changeStatus(@PathVariable UUID id,
            @RequestParam OrderStatus status) {
        OrderDTO updated = orderService.changeStatus(id, status);
        return ResponseEntity.ok(updated);
    }
    
    // endpoint el cual asigan conductor y recibir archivos PDF e Imagen
    // Usamos MULTIPART_FORM_DATA_VALUE porque viajan archivos binarios reales en la petición HTTP
    @PostMapping(value = "/{id}/assign-driver", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OrderDTO> assignDriver(
            @PathVariable UUID id,
            @RequestParam UUID driverId,
            @RequestPart("pdf") MultipartFile pdfFile,
            @RequestPart("image") MultipartFile imageFile) {
        
        try {
        	
        	// VALIDACION: Verificar si los archivos vienen nulos o completamente vacIos
            if (pdfFile == null || pdfFile.isEmpty()) {
                throw new RuntimeException("El archivo PDF es obligatorio y no puede estar vacio");
            }
            if (imageFile == null || imageFile.isEmpty()) {
                throw new RuntimeException("El archivo de imagen es obligatorio y no puede estar vacio");
            }
        	
            // convertimos los archivos recibidos a arreglos de bytes (byte[]) para enviarlos al servicio
            byte[] pdfBytes = pdfFile.getBytes();
            byte[] imgBytes = imageFile.getBytes();

            OrderDTO updated = orderService.assignDriverWithFiles(id, driverId, pdfBytes, imgBytes, imageFile.getContentType(), pdfFile.getContentType());
            return ResponseEntity.ok(updated);
            
        } catch (IOException e) {
            // si ocurre un fallo al leer los archivos, arrojamos un error
            throw new RuntimeException("Error al procesar los archivos adjuntos de la asignacion", e);
        }
    }
    
    // endpoint solicitado por el PDF para listar ordenes con filtros opcionales
    @GetMapping
    public ResponseEntity<java.util.List<OrderDTO>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String origin,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate date) {
        
        java.util.List<OrderDTO> orders = orderService.getOrdersWithFilters(status, origin, destination, date);
        return ResponseEntity.ok(orders);
    }
    
 // endpoint para consultar una orden especifica por su ID unico
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable UUID id) {
        OrderDTO order = orderService.getOrderById(id);
        return ResponseEntity.ok(order);
    }
    
}
