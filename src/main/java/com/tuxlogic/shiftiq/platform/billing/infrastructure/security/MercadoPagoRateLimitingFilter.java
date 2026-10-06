package com.tuxlogic.shiftiq.platform.billing.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory rate limiting filter protecting public and sensitive Mercado Pago endpoints
 * from abuse, denial of service, and preference spamming.
 */
@Component
public class MercadoPagoRateLimitingFilter extends OncePerRequestFilter {

    private static final int WEBHOOK_MAX_REQUESTS_PER_MINUTE = 120;
    private static final int PREFERENCE_MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_MILLIS = 60_000L;

    private static class RateLimitBucket {
        long windowStart;
        final AtomicInteger count = new AtomicInteger(0);

        RateLimitBucket(long windowStart) {
            this.windowStart = windowStart;
        }
    }

    private final Map<String, RateLimitBucket> rateLimitBuckets = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/v1/payments/mercadopago");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        int maxAllowed;
        String bucketKey;

        if (path.startsWith("/api/v1/payments/mercadopago/webhooks")) {
            maxAllowed = WEBHOOK_MAX_REQUESTS_PER_MINUTE;
            bucketKey = "webhook:" + clientIp;
        } else if (path.startsWith("/api/v1/payments/mercadopago/preferences")) {
            maxAllowed = PREFERENCE_MAX_REQUESTS_PER_MINUTE;
            bucketKey = "preference:" + clientIp;
        } else {
            filterChain.doFilter(request, response);
            return;
        }

        if (isRateLimited(bucketKey, maxAllowed)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private synchronized boolean isRateLimited(String bucketKey, int maxAllowed) {
        long now = System.currentTimeMillis();
        // Periodically purge old entries to avoid memory growth
        if (rateLimitBuckets.size() > 5000) {
            rateLimitBuckets.entrySet().removeIf(entry -> now - entry.getValue().windowStart > WINDOW_MILLIS * 2);
        }

        RateLimitBucket bucket = rateLimitBuckets.compute(bucketKey, (key, existing) -> {
            if (existing == null || (now - existing.windowStart) > WINDOW_MILLIS) {
                RateLimitBucket newBucket = new RateLimitBucket(now);
                newBucket.count.set(1);
                return newBucket;
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return bucket.count.get() > maxAllowed;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    void reset() {
        rateLimitBuckets.clear();
    }
}
