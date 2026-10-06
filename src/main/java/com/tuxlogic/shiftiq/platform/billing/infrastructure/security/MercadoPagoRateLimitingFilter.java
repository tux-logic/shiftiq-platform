package com.tuxlogic.shiftiq.platform.billing.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.resources.ErrorResource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory rate limiting filter protecting public and sensitive Mercado Pago endpoints
 * from abuse, denial of service, and preference spamming.
 */
public class MercadoPagoRateLimitingFilter extends OncePerRequestFilter {

    private static final int WEBHOOK_MAX_REQUESTS_PER_MINUTE = 120;
    private static final int PREFERENCE_MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_MILLIS = 60_000L;

    private static class RateLimitBucket {
        final AtomicLong windowStart;
        final AtomicInteger count = new AtomicInteger(1);

        RateLimitBucket(long windowStart) {
            this.windowStart = new AtomicLong(windowStart);
        }
    }

    private final Map<String, RateLimitBucket> rateLimitBuckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final MessageSource messageSource;

    public MercadoPagoRateLimitingFilter(ObjectMapper objectMapper, MessageSource messageSource) {
        this.objectMapper = objectMapper;
        this.messageSource = messageSource;
    }

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

            Locale locale = LocaleContextHolder.getLocale();
            String localizedMessage = messageSource.getMessage(
                    "billing.error.rateLimitExceeded",
                    null,
                    "Too many requests. Please try again later.",
                    locale
            );
            ErrorResource errorResource = new ErrorResource("TOO_MANY_REQUESTS", localizedMessage);
            response.getWriter().write(objectMapper.writeValueAsString(errorResource));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimited(String bucketKey, int maxAllowed) {
        long now = System.currentTimeMillis();

        // Non-blocking purge of expired buckets when map grows large
        if (rateLimitBuckets.size() > 5000) {
            rateLimitBuckets.entrySet().removeIf(entry -> now - entry.getValue().windowStart.get() > WINDOW_MILLIS * 2);
        }

        RateLimitBucket bucket = rateLimitBuckets.compute(bucketKey, (key, existing) -> {
            if (existing == null || (now - existing.windowStart.get()) > WINDOW_MILLIS) {
                return new RateLimitBucket(now);
            }
            existing.count.incrementAndGet();
            return existing;
        });

        return bucket.count.get() > maxAllowed;
    }

    /**
     * Resolves the remote IP address.
     * This filter never reads X-Forwarded-For: with server.forward-headers-strategy=native,
     * Tomcat's RemoteIpValve rewrites request.getRemoteAddr() before the security chain runs,
     * but only when the direct peer is a trusted proxy (server.tomcat.remoteip.internal-proxies)
     * and the value comes from the right-most untrusted hop. With no trusted proxy the raw
     * socket address is used, so an untrusted client cannot choose its own bucket key.
     */
    private String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        return (remoteAddr != null && !remoteAddr.isBlank()) ? remoteAddr : "unknown";
    }

    void reset() {
        rateLimitBuckets.clear();
    }
}
