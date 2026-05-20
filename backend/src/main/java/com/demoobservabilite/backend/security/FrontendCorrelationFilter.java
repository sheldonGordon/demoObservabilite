package com.demoobservabilite.backend.security;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FrontendCorrelationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontendCorrelationFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";
    private static final String SESSION_ID_HEADER = "X-Session-Id";
    private static final String RESOLVED_TRACE_ID_HEADER = "X-Resolved-Trace-Id";
    private static final String RESOLVED_SPAN_ID_HEADER = "X-Resolved-Span-Id";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[a-f0-9]{32}$");
    private static final Pattern SPAN_ID_PATTERN = Pattern.compile("^[a-f0-9]{16}$");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/films");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String previousTraceId = MDC.get("trace_id");
        String previousSpanId = MDC.get("span_id");
        String previousSessionId = MDC.get("frontend_session_id");
        long startNanos = System.nanoTime();

        try {
            String resolvedTraceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
            String resolvedSpanId = resolveSpanId(request.getHeader(SPAN_ID_HEADER), MDC.get("span_id"));
            MDC.put("trace_id", resolvedTraceId);
            MDC.put("traceId", resolvedTraceId);
            MDC.put("span_id", resolvedSpanId);
            MDC.put("spanId", resolvedSpanId);
            response.setHeader(RESOLVED_TRACE_ID_HEADER, resolvedTraceId);
            response.setHeader(RESOLVED_SPAN_ID_HEADER, resolvedSpanId);

            String incomingSessionId = request.getHeader(SESSION_ID_HEADER);
            if (incomingSessionId != null && !incomingSessionId.isBlank() && incomingSessionId.length() <= 64) {
                MDC.put("frontend_session_id", incomingSessionId);
            }

            filterChain.doFilter(request, response);
            long durationMs = (System.nanoTime() - startNanos) / 1_000_000;
            LOGGER.info("api films method={} uri={} status={} durationMs={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs);
        } finally {
            restoreMdc("trace_id", previousTraceId);
            restoreMdc("traceId", previousTraceId);
            restoreMdc("span_id", previousSpanId);
            restoreMdc("spanId", previousSpanId);
            restoreMdc("frontend_session_id", previousSessionId);
        }
    }

    private static String resolveTraceId(String incomingTraceId) {
        if (incomingTraceId != null && TRACE_ID_PATTERN.matcher(incomingTraceId).matches()) {
            return incomingTraceId;
        }

        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String resolveSpanId(String incomingSpanId, String currentSpanId) {
        if (incomingSpanId != null && SPAN_ID_PATTERN.matcher(incomingSpanId).matches()) {
            return incomingSpanId;
        }
        if (currentSpanId != null && SPAN_ID_PATTERN.matcher(currentSpanId).matches()) {
            return currentSpanId;
        }

        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    private static void restoreMdc(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, previousValue);
    }
}
