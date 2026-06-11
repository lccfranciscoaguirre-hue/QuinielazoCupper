package org.mundial.quiniela.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            return Map.of("success", true, "message", "Login exitoso");
        } else {
            return Map.of("success", false, "message", "Credenciales incorrectas");
        }
    }
}
