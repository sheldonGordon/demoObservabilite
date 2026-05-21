package com.demoobservabilite.backend.controller;

import java.util.Collections;
import java.util.Map;

import com.demoobservabilite.backend.controller.dto.AuthRequest;
import com.demoobservabilite.backend.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    
    private final JwtService jwtService;

    @Value("${security.demo-user.username:demo}")
    private String demoUsername;

    @Value("${security.demo-user.password:demo}")
    private String demoPassword;

    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/token")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> token(@RequestBody AuthRequest authRequest) {
        LOGGER.info("Requete d'authentification recue pour l'utilisateur: {}", 
                authRequest != null ? authRequest.getUsername() : "null");
        
        if (authRequest == null
                || authRequest.getUsername() == null
                || authRequest.getPassword() == null
                || !demoUsername.equals(authRequest.getUsername())
                || !demoPassword.equals(authRequest.getPassword())) {
            LOGGER.warn("Authentification echouee pour l'utilisateur: {}", 
                    authRequest != null ? authRequest.getUsername() : "null");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(authRequest.getUsername());
        LOGGER.info("Token JWT genere avec succes pour l'utilisateur: {}", authRequest.getUsername());
        return Collections.singletonMap("token", token);
    }
}

