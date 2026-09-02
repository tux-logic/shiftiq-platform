package com.tuxlogic.shiftiq.platform.billing.infrastructure.outbound.factos;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FactosGateway;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.VoucherType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of the FactosGateway outbound service interface.
 * ACL client connecting to the external Factos Electronic Invoicing REST API.
 */
@Service
public class FactosGatewayImpl implements FactosGateway {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactosGatewayImpl.class);
    private static final AtomicLong CORRELATIVE_COUNTER = new AtomicLong((System.currentTimeMillis() / 1000) % 100000000L);

    private final String factosApiUrl;
    private final String factosApiKey;
    private final RestTemplate restTemplate;

    public FactosGatewayImpl(
            @Value("${factos.api.url}") String factosApiUrl,
            @Value("${factos.api.key}") String factosApiKey,
            RestTemplate restTemplate) {
        this.factosApiUrl = factosApiUrl;
        this.factosApiKey = factosApiKey;
        this.restTemplate = restTemplate;
    }

    @Override
    public Optional<FactosInvoiceResult> issueVoucher(
            String issuerRuc,
            VoucherType documentType,
            String customerDocumentType,
            String customerDocumentNumber,
            String customerName,
            List<FactosItem> items
    ) {
        try {
            String cpeType = documentType == VoucherType.INVOICE ? "01" : "03";
            String series = documentType == VoucherType.INVOICE ? "F001" : "B001";
            String correlative = String.format("%08d", CORRELATIVE_COUNTER.getAndIncrement() % 100000000L);
            String issueDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            List<FactosIssueInvoiceRequest.Item> requestItems = items.stream()
                    .map(item -> new FactosIssueInvoiceRequest.Item(
                            item.code() != null ? item.code() : "SERV-001",
                            item.description(),
                            item.quantity(),
                            item.unitPrice(),
                            "TAXABLE_ONEROUS"
                    ))
                    .toList();

            var requestDto = new FactosIssueInvoiceRequest(
                    series,
                    correlative,
                    cpeType,
                    issueDate,
                    issuerRuc,
                    customerDocumentNumber,
                    customerName,
                    "PEN",
                    requestItems
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", factosApiKey);

            HttpEntity<FactosIssueInvoiceRequest> requestEntity = new HttpEntity<>(requestDto, headers);

            String endpoint = factosApiUrl.endsWith("/") ? factosApiUrl + "api/v1/documents" : factosApiUrl + "/api/v1/documents";

            var response = restTemplate.postForObject(
                    endpoint,
                    requestEntity,
                    FactosIssueInvoiceResponse.class
            );

            if (response != null && "EMITTED".equalsIgnoreCase(response.status())) {
                String pdfUrl = response.pdfUrl();
                if (pdfUrl != null && !pdfUrl.startsWith("http://") && !pdfUrl.startsWith("https://")) {
                    String baseUrl = factosApiUrl.endsWith("/") ? factosApiUrl.substring(0, factosApiUrl.length() - 1) : factosApiUrl;
                    pdfUrl = baseUrl + (pdfUrl.startsWith("/") ? pdfUrl : "/" + pdfUrl);
                }
                return Optional.of(new FactosInvoiceResult(
                        response.series(),
                        response.correlative(),
                        pdfUrl,
                        response.totalAmount()
                ));
            }

            return Optional.empty();
        } catch (RestClientException e) {
            LOGGER.error("Error calling Factos Service: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }
}
