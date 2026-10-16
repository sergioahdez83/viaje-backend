package com.viaje.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.viaje.api.client.DriverClient;
import com.viaje.api.dto.DriverDTO;
import com.viaje.api.dto.OrderDTO;
import com.viaje.api.model.Order;
import com.viaje.api.model.OrderStatus;
import com.viaje.api.repository.OrderRepository;
import com.viaje.api.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderServiceTest {
	
	 @Mock
	    private OrderRepository orderRepository;
	 
	 @Mock
	    private DriverClient driverClient;

	    @InjectMocks
	    private OrderService orderService;

	    private UUID orderId;
	    private UUID driverId;
	    private Order mockOrder;
	    private DriverDTO mockDriverDTO;

	    @BeforeEach
	    void setUp() {
	        MockitoAnnotations.openMocks(this);
	        orderId = UUID.randomUUID();
	        driverId = UUID.randomUUID();
	        
	        mockOrder = new Order();
	        mockOrder.setId(orderId);
	        mockOrder.setStatus(OrderStatus.CREATED);
	        mockOrder.setOrigin("Mazatlan");
	        mockOrder.setDestination("Ciudad de Mexico");
	        mockOrder.setCreatedAt(LocalDateTime.now());
	        mockOrder.setUpdatedAt(LocalDateTime.now());
	        
	     // Configuración del conductor virtual que responderá OpenFeign por red
	        mockDriverDTO = new DriverDTO(driverId, "Sergio Hernandez", "LIC-2026-SH", true);
	    }

	    @Test
	    void cambioStatus() {
	        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
	        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

	        OrderDTO result = orderService.changeStatus(orderId, OrderStatus.IN_TRANSIT);

	        assertNotNull(result);
	        assertEquals(OrderStatus.IN_TRANSIT, result.getStatus());
	        verify(orderRepository, times(1)).save(any(Order.class));
	    }

	    @Test
	    void testChangeStatus_InvalidTransition_ThrowsException() {
	        mockOrder.setStatus(OrderStatus.DELIVERED);
	        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

	        assertThrows(RuntimeException.class, () -> {
	            orderService.changeStatus(orderId, OrderStatus.CANCELLED);
	        });

	        verify(orderRepository, never()).save(any(Order.class));
	    }

	    // CONSULTA INDIVIDUAL POR ID
	    @Test
	    void getOrderById() {
	        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

	        OrderDTO result = orderService.getOrderById(orderId);
	        
	        System.out.println(" RESULTADO DE LA CONSULTA RECUPERADO DE MEMORIA:");
	        System.out.println("   -> ID de Orden: " + result.getId());
	        System.out.println("   -> Estado Actual: " + result.getStatus());
	        System.out.println("   -> Ruta: [" + result.getOrigin() + "] hacia [" + result.getDestination() + "]");

	        assertNotNull(result);
	        assertEquals("Mazatlan", result.getOrigin());
	        assertEquals("Ciudad de Mexico", result.getDestination());
	        assertEquals(orderId, result.getId());
	        assertEquals(OrderStatus.CREATED, result.getStatus());
	        verify(orderRepository, times(1)).findById(orderId);
	    }

	    // LISTADO 
	    @Test
	    void testGetOrdersConFiltros() {
	        List<Order> mockList = new ArrayList<>();
	        mockList.add(mockOrder);
	        
	        // Simulamos que al ejecutar el findAll con cualquier Specification, nos regresa la lista virtual
	        when(orderRepository.findAll(any(Specification.class))).thenReturn(mockList);

	        List<OrderDTO> result = orderService.getOrdersWithFilters(OrderStatus.CREATED, "Mazatlan", null, null);
	        
	        System.out.println("RESULTADOS DE LA CONSULTA FILTRADA ENCONTRADOS (" + result.size() + "):");
	        for (OrderDTO dto : result) {
	            System.out.println("   -> [Orden Encontrada] ID: " + dto.getId() + " | Estado: " + dto.getStatus() + " | Origen: " + dto.getOrigin());
	        }

	        assertNotNull(result);
	        assertFalse(result.isEmpty());
	        assertEquals(1, result.size());
	        assertEquals("Mazatlan", result.get(0).getOrigin());
	        verify(orderRepository, times(1)).findAll(any(Specification.class));
	    }
	    
	    
	    // pdf e imagen
	    @Test
	    void assignDriverWithFiles() {

	        // "encuentra" al conductor activo y el repositorio encuentra la orden
	        when(driverClient.getDriverById(driverId)).thenReturn(mockDriverDTO);
	        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
	        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

	        // simulamos la carga de los archivos en arreglos de bytes 
	        byte[] pdfSimulado = "%PDF-1.4 Guia de Viaje Binaria Corporativa".getBytes();
	        byte[] imagenSimulada = "[PNG DATA BYTES] Evidencia de Firma".getBytes();

	        // ejcución del metodo transaccional de negocio
	        OrderDTO result = orderService.assignDriverWithFiles(
	                orderId, 
	                driverId, 
	                pdfSimulado, 
	                imagenSimulada, 
	                "image/png", 
	                "application/pdf"
	        );

	        // Imprimir la simulación del almacenamiento binario exitoso en consola
	        System.out.println(" SE PROCESA LA CARGA DE LOLS ARCHIVOS:");
	        System.out.println("   -> ID Orden Impactada: " + result.getId());
	        System.out.println("   -> ID Conductor Asignado por Feign: " + result.getDriverId());
	        System.out.println("   -> Tamaño del PDF guardado virtualmente: " + pdfSimulado.length + " bytes");
	        System.out.println("   -> Tamaño de la Imagen guardada virtualmente: " + imagenSimulada.length + " bytes");
	        System.out.println("   -> Nuevo Estado del Viaje en base de datos: " + result.getStatus());

	        assertNotNull(result);
	        assertEquals(OrderStatus.IN_TRANSIT, result.getStatus());
	        assertEquals(driverId, result.getDriverId());
	        verify(orderRepository, times(1)).save(any(Order.class));
	        System.out.println("========================================================\n");
	    }
	    
	    //  error controlado ante de archivos erroneos
	    @Test
	    void assignDriverWithFiles_Fallo() {
	    	
	    	 System.out.println("========================================================\n");

	        when(driverClient.getDriverById(driverId)).thenReturn(mockDriverDTO);
	        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

	        // Forzamos un Content-Type prohibido como "text/plain" o "image/gif"
	        String formatoProhibido = "image/gif";

	        System.out.println(" disparando asignacion con formato no permitido: " + formatoProhibido);
	        
	        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
	            orderService.assignDriverWithFiles(
	                    orderId, driverId, "PDF".getBytes(), "IMG".getBytes(), formatoProhibido, "application/pdf"
	            );
	        });

	        // Capturamos e imprimimos el error arrojado por tu lógica de negocio
	        System.out.println(" ERROR CONTROLADO ARROJADO POR EL BACKEND:");
	        System.out.println("   -> Mensaje: \"" + exception.getMessage() + "\"");
	        
	        // Verificamos que jamás se guardó en el repositorio para no corromper datos
	        verify(orderRepository, never()).save(any(Order.class));
	        System.out.println("========================================================\n");
	    }
	    
	 //  error controlado ante una transicion de tstado Invalido (ej. CREATED directo a DELIVERED)
	    @Test
	    void testChangeStatus_Failed_InvalidTransition() {

	        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

	        System.out.println(" saltar flujo de negocio: CREATED -> DELIVERED");

	        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
	            orderService.changeStatus(orderId, OrderStatus.DELIVERED);
	        });

	        System.out.println(" ERROR CONTROLADO:");
	        System.out.println("   -> Mensaje: \"" + exception.getMessage() + "\"");

	        verify(orderRepository, never()).save(any(Order.class));
	        System.out.println("========================================================\n");
	    }

}
