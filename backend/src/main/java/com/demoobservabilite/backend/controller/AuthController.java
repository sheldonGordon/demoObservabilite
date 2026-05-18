package com.demoobservabilite.backend.controller;

import java.util.Collections;
import java.util.Map;

import com.demoobservabilite.backend.controller.dto.AuthRequest;
import com.demoobservabilite.backend.security.JwtService;
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
        if (authRequest == null
                || authRequest.getUsername() == null
                || authRequest.getPassword() == null
                || !demoUsername.equals(authRequest.getUsername())
                || !demoPassword.equals(authRequest.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        String token = jwtService.generateToken(authRequest.getUsername());
        return Collections.singletonMap("token", token);
    }
}

