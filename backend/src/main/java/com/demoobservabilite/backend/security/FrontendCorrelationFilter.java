package com.demoobservabilite.backend.security;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FrontendCorrelationFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SESSION_ID_HEADER = "X-Session-Id";
    private static final String RESOLVED_TRACE_ID_HEADER = "X-Resolved-Trace-Id";
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[a-f0-9]{32}$");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !uri.startsWith("/api/films");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String previousTraceId = MDC.get("trace_id");
        String previousSessionId = MDC.get("frontend_session_id");

        try {
            String resolvedTraceId = resolveTraceId(request.getHeader(TRACE_ID_HEADER));
            MDC.put("trace_id", resolvedTraceId);
            response.setHeader(RESOLVED_TRACE_ID_HEADER, resolvedTraceId);

            String incomingSessionId = request.getHeader(SESSION_ID_HEADER);
            if (incomingSessionId != null && !incomingSessionId.isBlank() && incomingSessionId.length() <= 64) {
                MDC.put("frontend_session_id", incomingSessionId);
            }

            filterChain.doFilter(request, response);
        } finally {
            restoreMdc("trace_id", previousTraceId);
            restoreMdc("frontend_session_id", previousSessionId);
        }
    }

    private static String resolveTraceId(String incomingTraceId) {
        if (incomingTraceId != null && TRACE_ID_PATTERN.matcher(incomingTraceId).matches()) {
            return incomingTraceId;
        }

        // Fallback server-side trace id to keep logs correlated even without frontend header.
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static void restoreMdc(String key, String previousValue) {
        if (previousValue == null) {
            MDC.remove(key);
            return;
        }
        MDC.put(key, previousValue);
    }
}

