package com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.CompanyIdentityResult;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.PersonIdentityResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.Optional;

/**
 * Adapter implementation of IdentityValidationService communicating with Factiliza REST API (RENIEC, SUNAT, Migraciones).
 */
@Service
public class FactilizaClient implements IdentityValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FactilizaClient.class);
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration READ_TIMEOUT = Duration.ofSeconds(5);

    private final String apiUrl;
    private final String apiToken;
    private final RestClient restClient;

    public FactilizaClient(
            @Value("${factiliza.api.url:https://api.factiliza.com}") String apiUrl,
            @Value("${factiliza.api.token:}") String apiToken,
            RestClient.Builder restClientBuilder
    ) {
        this.apiUrl = apiUrl != null && apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        this.apiToken = apiToken;
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(CONNECT_TIMEOUT);
        requestFactory.setReadTimeout(READ_TIMEOUT);
        this.restClient = restClientBuilder
                .requestFactory(requestFactory)
                .build();
    }

    public FactilizaClient(String apiUrl, String apiToken, RestClient restClient) {
        this.apiUrl = apiUrl != null && apiUrl.endsWith("/") ? apiUrl.substring(0, apiUrl.length() - 1) : apiUrl;
        this.apiToken = apiToken;
        this.restClient = restClient;
    }

    @Override
    public Optional<PersonIdentityResult> validateDni(String dni) {
        if (dni == null || !dni.matches("^[0-9]{8}$")) {
            LOGGER.debug("Invalid DNI format: {}", dni);
            return Optional.empty();
        }

        if (apiToken == null || apiToken.isBlank()) {
            LOGGER.warn("Factiliza API token is not configured. Simulating DNI lookup for: {}", dni);
            return Optional.of(new PersonIdentityResult(
                    "DNI", dni, "CIUDADANO", "SIMULADO", "SIMULADO, CIUDADANO",
                    "AV. SIMULADA 123", "LIMA", "LIMA", "MIRAFLORES", "0", true
            ));
        }

        try {
            var response = restClient.get()
                    .uri(apiUrl + "/v1/dni/info/{dni}", dni)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        LOGGER.debug("Factiliza DNI lookup 4xx response: {}", clientResponse.getStatusCode());
                    })
                    .body(FactilizaDniResponse.class);

            if (response != null && Boolean.TRUE.equals(response.success()) && response.data() != null) {
                var data = response.data();
                String lastName = formatLastName(data.apellidoPaterno(), data.apellidoMaterno());
                return Optional.of(new PersonIdentityResult(
                        "DNI",
                        data.numero() != null ? data.numero() : dni,
                        data.nombres(),
                        lastName,
                        data.nombreCompleto() != null ? data.nombreCompleto() : (data.nombres() + " " + lastName),
                        data.direccionCompleta() != null ? data.direccionCompleta() : data.direccion(),
                        data.departamento(),
                        data.provincia(),
                        data.distrito(),
                        data.codigoVerificacion(),
                        true
                ));
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound ex) {
            LOGGER.debug("DNI {} not found in Factiliza/RENIEC", dni);
            return Optional.empty();
        } catch (Exception ex) {
            LOGGER.error("Failed to query Factiliza DNI endpoint for {}: {}", dni, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<PersonIdentityResult> validateCe(String ce) {
        if (ce == null || ce.isBlank() || ce.length() < 7 || ce.length() > 12) {
            LOGGER.debug("Invalid CE format: {}", ce);
            return Optional.empty();
        }

        if (apiToken == null || apiToken.isBlank()) {
            LOGGER.warn("Factiliza API token is not configured. Simulating CE lookup for: {}", ce);
            return Optional.of(new PersonIdentityResult(
                    "CE", ce, "EXTRANJERO", "SIMULADO", "SIMULADO, EXTRANJERO",
                    null, null, null, null, null, true
            ));
        }

        try {
            var response = restClient.get()
                    .uri(apiUrl + "/pe/v1/cee/info/{cee}", ce)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        LOGGER.debug("Factiliza CE lookup 4xx response: {}", clientResponse.getStatusCode());
                    })
                    .body(FactilizaCeResponse.class);

            if (response != null && Boolean.TRUE.equals(response.success()) && response.data() != null) {
                var data = response.data();
                String lastName = formatLastName(data.apellidoPaterno(), data.apellidoMaterno());
                return Optional.of(new PersonIdentityResult(
                        "CE",
                        data.numero() != null ? data.numero() : ce,
                        data.nombres(),
                        lastName,
                        data.nombreCompleto() != null ? data.nombreCompleto() : (data.nombres() + " " + lastName),
                        null,
                        null,
                        null,
                        null,
                        null,
                        true
                ));
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound ex) {
            LOGGER.debug("CE {} not found in Factiliza/Migraciones", ce);
            return Optional.empty();
        } catch (Exception ex) {
            LOGGER.error("Failed to query Factiliza CE endpoint for {}: {}", ce, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<CompanyIdentityResult> validateRuc(String ruc) {
        if (ruc == null || !ruc.matches("^[0-9]{11}$")) {
            LOGGER.debug("Invalid RUC format: {}", ruc);
            return Optional.empty();
        }

        if (apiToken == null || apiToken.isBlank()) {
            LOGGER.warn("Factiliza API token is not configured. Simulating RUC lookup for: {}", ruc);
            return Optional.of(new CompanyIdentityResult(
                    ruc, "EMPRESA SIMULADA S.A.C.", "ACTIVO", "HABIDO",
                    "AV. EMPRESARIAL 456", "LIMA", "LIMA", "SAN ISIDRO", "150131",
                    true, true, true
            ));
        }

        try {
            var response = restClient.get()
                    .uri(apiUrl + "/v1/ruc/info/{ruc}", ruc)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::is4xxClientError, (request, clientResponse) -> {
                        LOGGER.debug("Factiliza RUC lookup 4xx response: {}", clientResponse.getStatusCode());
                    })
                    .body(FactilizaRucResponse.class);

            if (response != null && Boolean.TRUE.equals(response.success()) && response.data() != null) {
                var data = response.data();
                boolean active = "ACTIVO".equalsIgnoreCase(data.estado());
                boolean habido = "HABIDO".equalsIgnoreCase(data.condicion());
                return Optional.of(new CompanyIdentityResult(
                        data.numero() != null ? data.numero() : ruc,
                        data.nombreORazonSocial(),
                        data.estado(),
                        data.condicion(),
                        data.direccionCompleta() != null ? data.direccionCompleta() : data.direccion(),
                        data.departamento(),
                        data.provincia(),
                        data.distrito(),
                        data.ubigeoSunat(),
                        active,
                        habido,
                        true
                ));
            }
            return Optional.empty();
        } catch (HttpClientErrorException.NotFound ex) {
            LOGGER.debug("RUC {} not found in Factiliza/SUNAT", ruc);
            return Optional.empty();
        } catch (Exception ex) {
            LOGGER.error("Failed to query Factiliza RUC endpoint for {}: {}", ruc, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public Optional<PersonIdentityResult> validatePerson(String documentType, String documentNumber) {
        if (documentType == null || documentNumber == null) {
            return Optional.empty();
        }
        String normalized = documentType.trim().toUpperCase();
        return switch (normalized) {
            case "DNI" -> validateDni(documentNumber.trim());
            case "CE", "CEE" -> validateCe(documentNumber.trim());
            default -> Optional.empty();
        };
    }

    private String formatLastName(String apellidoPaterno, String apellidoMaterno) {
        if (apellidoPaterno != null && apellidoMaterno != null) {
            return (apellidoPaterno.trim() + " " + apellidoMaterno.trim()).trim();
        }
        if (apellidoPaterno != null) {
            return apellidoPaterno.trim();
        }
        if (apellidoMaterno != null) {
            return apellidoMaterno.trim();
        }
        return "";
    }
}
