package com.demoobservabilite.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final FrontendCorrelationFilter frontendCorrelationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            FrontendCorrelationFilter frontendCorrelationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.frontendCorrelationFilter = frontendCorrelationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/auth/token", "/actuator", "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/logs/frontend").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/hello").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(frontendCorrelationFilter, JwtAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

