package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.factos;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FactosGateway;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactosGatewayImplTest {

    @Mock
    private RestTemplate restTemplate;

    private FactosGatewayImpl factosGateway;

    private static final String API_URL = "https://factos-reva.onrender.com";
    private static final String API_KEY = "test_api_key_123";

    @BeforeEach
    void setUp() {
        factosGateway = new FactosGatewayImpl(API_URL, API_KEY, restTemplate);
    }

    @Test
    void issueVoucher_WhenApiReturnsEmitted_ShouldReturnInvoiceResult() {
        // Arrange
        FactosIssueInvoiceResponse response = new FactosIssueInvoiceResponse(
                "F001",
                "00000001",
                "01",
                "2026-08-31",
                "20123456789",
                "20111222333",
                "Empresa Test",
                "EMITTED",
                new BigDecimal("100.00"),
                new BigDecimal("18.00"),
                new BigDecimal("118.00"),
                "PEN",
                "/api/v1/rendering/pdf/F001/00000001",
                List.of()
        );

        when(restTemplate.postForObject(
                eq("https://factos-reva.onrender.com/api/v1/comprobantes"),
                any(HttpEntity.class),
                eq(FactosIssueInvoiceResponse.class)
        )).thenReturn(response);

        List<FactosGateway.FactosItem> items = List.of(
                new FactosGateway.FactosItem("SERV-001", "Servicio Test", BigDecimal.ONE, new BigDecimal("118.00"))
        );

        // Act
        Optional<FactosGateway.FactosInvoiceResult> result = factosGateway.issueVoucher(
                "20123456789",
                VoucherType.INVOICE,
                "6",
                "20111222333",
                "Empresa Test",
                items
        );

        // Assert
        assertTrue(result.isPresent());
        assertEquals("F001", result.get().series());
        assertEquals("00000001", result.get().correlative());
        assertEquals("/api/v1/rendering/pdf/F001/00000001", result.get().pdfUrl());
        assertEquals(new BigDecimal("118.00"), result.get().totalAmount());
    }

    @Test
    void issueVoucher_WhenApiFails_ShouldReturnEmpty() {
        // Arrange
        when(restTemplate.postForObject(
                anyString(),
                any(HttpEntity.class),
                eq(FactosIssueInvoiceResponse.class)
        )).thenThrow(new org.springframework.web.client.RestClientException("Connection refused"));

        List<FactosGateway.FactosItem> items = List.of(
                new FactosGateway.FactosItem("SERV-001", "Servicio Test", BigDecimal.ONE, new BigDecimal("118.00"))
        );

        // Act
        Optional<FactosGateway.FactosInvoiceResult> result = factosGateway.issueVoucher(
                "20123456789",
                VoucherType.INVOICE,
                "6",
                "20111222333",
                "Empresa Test",
                items
        );

        // Assert
        assertTrue(result.isEmpty());
    }
}
