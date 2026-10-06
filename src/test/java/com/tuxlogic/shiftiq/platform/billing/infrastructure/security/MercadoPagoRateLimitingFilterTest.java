package com.tuxlogic.shiftiq.platform.billing.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MercadoPagoRateLimitingFilterTest {

    private MercadoPagoRateLimitingFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new MercadoPagoRateLimitingFilter();
        filter.reset();
        filterChain = mock(FilterChain.class);
    }

    @Test
    void doFilter_WhenNonMercadoPagoPath_ShouldPassThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilter_WhenWebhookUnderLimit_ShouldPassThrough() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/payments/mercadopago/webhooks");
        request.setRemoteAddr("192.168.1.1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilter_WhenWebhookExceedsLimit_ShouldReturn429() throws ServletException, IOException {
        String clientIp = "10.0.0.5";

        for (int i = 0; i < 120; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI("/api/v1/payments/mercadopago/webhooks");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(req, resp, filterChain);
            assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        }

        // 121st request should be rate limited
        MockHttpServletRequest reqLimit = new MockHttpServletRequest();
        reqLimit.setRequestURI("/api/v1/payments/mercadopago/webhooks");
        reqLimit.setRemoteAddr(clientIp);
        MockHttpServletResponse respLimit = new MockHttpServletResponse();
        filter.doFilter(reqLimit, respLimit, filterChain);

        assertEquals(429, respLimit.getStatus());
        assertEquals("60", respLimit.getHeader("Retry-After"));
        assertTrue(respLimit.getContentAsString().contains("Too many requests"));
    }

    @Test
    void doFilter_WhenPreferencesExceedsLimit_ShouldReturn429() throws ServletException, IOException {
        String clientIp = "10.0.0.6";

        for (int i = 0; i < 30; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI("/api/v1/payments/mercadopago/preferences");
            req.setRemoteAddr(clientIp);
            MockHttpServletResponse resp = new MockHttpServletResponse();
            filter.doFilter(req, resp, filterChain);
            assertEquals(HttpServletResponse.SC_OK, resp.getStatus());
        }

        // 31st request should be rate limited
        MockHttpServletRequest reqLimit = new MockHttpServletRequest();
        reqLimit.setRequestURI("/api/v1/payments/mercadopago/preferences");
        reqLimit.setRemoteAddr(clientIp);
        MockHttpServletResponse respLimit = new MockHttpServletResponse();
        filter.doFilter(reqLimit, respLimit, filterChain);

        assertEquals(429, respLimit.getStatus());
        assertEquals("60", respLimit.getHeader("Retry-After"));
    }

    @Test
    void doFilter_WhenXForwardedForHeaderPresent_ShouldUseClientIp() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/v1/payments/mercadopago/webhooks");
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
