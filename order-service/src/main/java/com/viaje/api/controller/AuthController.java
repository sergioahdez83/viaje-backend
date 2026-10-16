package com.viaje.api.controller;

import com.viaje.api.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
	
	private final JwtUtil jwtUtil;

    public AuthController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    // endpoint para obtener un token JWT de prueba
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestParam String username) {

        // se genera el token directo para agilizar las pruebas
        String token = jwtUtil.generateToken(username);
        
        Map<String, String> response = new HashMap<>();
        response.put("token", token);
        response.put("token_type", "Bearer");
        
        return ResponseEntity.ok(response);
    }

}
