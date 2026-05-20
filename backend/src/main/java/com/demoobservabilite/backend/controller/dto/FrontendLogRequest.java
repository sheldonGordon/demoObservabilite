package com.demoobservabilite.backend.controller.dto;

import java.time.OffsetDateTime;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FrontendLogRequest(
        @NotBlank @Pattern(regexp = "^[a-f0-9]{32}$") String traceId,
        @NotBlank @Pattern(regexp = "^[a-f0-9]{16}$") String spanId,
        @NotBlank @Size(max = 64) String sessionId,
        @NotBlank @Pattern(regexp = "^(DEBUG|INFO|WARN|ERROR)$") String level,
        @NotBlank @Size(max = 80) String event,
        @NotBlank @Size(max = 1000) String message,
        @NotNull OffsetDateTime timestamp,
        @Size(max = 200) String route,
        @Size(max = 300) String userAgent,
        Map<String, Object> context) {
}
