package com.demoobservabilite.backend.controller;

import java.util.Map;

import com.demoobservabilite.backend.controller.dto.FrontendLogRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs/frontend")
public class FrontendLogController {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontendLogController.class);

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void ingestFrontendLog(@Valid @RequestBody FrontendLogRequest request) {
        String previousTraceId = MDC.get("trace_id");
        String previousSpanId = MDC.get("span_id");
        String previousSessionId = MDC.get("frontend_session_id");

        try {
            MDC.put("trace_id", request.traceId());
            MDC.put("traceId", request.traceId());
            MDC.put("span_id", request.spanId());
            MDC.put("spanId", request.spanId());
            MDC.put("frontend_session_id", request.sessionId());
            logByLevel(request);
        } finally {
            restoreMdc("trace_id", previousTraceId);
            restoreMdc("traceId", previousTraceId);
            restoreMdc("span_id", previousSpanId);
            restoreMdc("spanId", previousSpanId);
            restoreMdc("frontend_session_id", previousSessionId);
        }
    }

    private void logByLevel(FrontendLogRequest request) {
        String sanitizedMessage = sanitize(request.message());
        Map<String, Object> context = request.context() == null ? Map.of() : request.context();

        switch (request.level()) {
            case "DEBUG" -> LOGGER.debug(
                    "frontend event={} route={} timestamp={} message={} userAgent={} context={}",
                    request.event(),
                    request.route(),
                    request.timestamp(),
                    sanitizedMessage,
                    request.userAgent(),
                    context);
            case "WARN" -> LOGGER.warn(
                    "frontend event={} route={} timestamp={} message={} userAgent={} context={}",
                    request.event(),
                    request.route(),
                    request.timestamp(),
                    sanitizedMessage,
                    request.userAgent(),
                    context);
            case "ERROR" -> LOGGER.error(
                    "frontend event={} route={} timestamp={} message={} userAgent={} context={}",
                    request.event(),
                    request.route(),
                    request.timestamp(),
                    sanitizedMessage,
                    request.userAgent(),
                    context);
            default -> LOGGER.info(
                    "frontend event={} route={} timestamp={} message={} userAgent={} context={}",
                    request.event(),
                    request.route(),
                    request.timestamp(),
                    sanitizedMessage,
                    request.userAgent(),
                    context);
        }
    }

    private static String sanitize(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ');
    }

    private static void restoreMdc(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, previousValue);
    }
}
