package com.demoobservabilite.backend.controller;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api")
public class GreetingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GreetingController.class);

    @GetMapping("/hello")
    public Map<String, Object> hello() {
        LOGGER.info("Requete recue sur /api/hello");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "backend");
        response.put("message", "Bonjour depuis le backend Spring Boot");
        response.put("timestamp", Instant.now().toString());
        return response;
    }

    @GetMapping("/secure/hello")
    public Map<String, Object> secureHello(Authentication authentication) {
        LOGGER.info("Requete recue sur /api/secure/hello");
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("application", "backend");
        response.put("message", "Bonjour depuis l'endpoint securise");
        response.put("user", authentication != null ? authentication.getName() : "unknown");
        response.put("timestamp", Instant.now().toString());
        return response;
    }
}

