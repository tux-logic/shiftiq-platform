package com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class FactilizaClientTest {

    private static final String API_URL = "https://api.factiliza.com";
    private static final String API_TOKEN = "test_factiliza_token_abc";

    private MockRestServiceServer mockServer;
    private FactilizaClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mockServer = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        client = new FactilizaClient(API_URL, API_TOKEN, restClient);
    }

    @Test
    @DisplayName("validateDni returns person details when RENIEC record exists")
    void validateDni_WhenValidResponse_ReturnsPersonResult() {
        String dni = "72345678";
        String jsonResponse = """
                {
                  "status": 200,
                  "message": "Exito",
                  "success": true,
                  "data": {
                    "numero": "72345678",
                    "codigo_verificacion": "4",
                    "nombres": "JAZMIN",
                    "apellido_paterno": "PAUCAR",
                    "apellido_materno": "TTUPA",
                    "nombre_completo": "PAUCAR TTUPA, JAZMIN",
                    "departamento": "CUSCO",
                    "provincia": "CALCA",
                    "distrito": "YANATILE",
                    "direccion_completa": "COLCA - YANATILE, CUSCO"
                  }
                }
                """;

        mockServer.expect(requestTo(API_URL + "/v1/dni/info/" + dni))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + API_TOKEN))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        var result = client.validateDni(dni);

        assertTrue(result.isPresent());
        var person = result.get();
        assertEquals("DNI", person.documentType());
        assertEquals(dni, person.documentNumber());
        assertEquals("JAZMIN", person.firstName());
        assertEquals("PAUCAR TTUPA", person.lastName());
        assertEquals("4", person.verificationDigit());
        assertTrue(person.valid());
        mockServer.verify();
    }

    @Test
    @DisplayName("validateDni returns empty when DNI is not found (404)")
    void validateDni_WhenNotFound_ReturnsEmpty() {
        String dni = "00000000";

        mockServer.expect(requestTo(API_URL + "/v1/dni/info/" + dni))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        var result = client.validateDni(dni);

        assertTrue(result.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("validateDni returns empty without API call when format is invalid")
    void validateDni_WhenMalformedFormat_ReturnsEmptyWithoutApiCall() {
        var result1 = client.validateDni("123");
        var result2 = client.validateDni("abcdefgh");
        var result3 = client.validateDni(null);

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
    }

    @Test
    @DisplayName("validateRuc returns company details when SUNAT record is active and habido")
    void validateRuc_WhenActiveAndHabido_ReturnsCompanyResult() {
        String ruc = "20552103816";
        String jsonResponse = """
                {
                  "status": 200,
                  "message": "Exito",
                  "success": true,
                  "data": {
                    "numero": "20552103816",
                    "nombre_o_razon_social": "AGROLIGHT PERU S.A.C.",
                    "estado": "ACTIVO",
                    "condicion": "HABIDO",
                    "departamento": "LIMA",
                    "provincia": "LIMA",
                    "distrito": "SANTA ANITA",
                    "direccion_completa": "PJ. JORGE BASADRE NRO. 158",
                    "ubigeo_sunat": "150137"
                  }
                }
                """;

        mockServer.expect(requestTo(API_URL + "/v1/ruc/info/" + ruc))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + API_TOKEN))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        var result = client.validateRuc(ruc);

        assertTrue(result.isPresent());
        var company = result.get();
        assertEquals(ruc, company.ruc());
        assertEquals("AGROLIGHT PERU S.A.C.", company.businessName());
        assertEquals("ACTIVO", company.status());
        assertEquals("HABIDO", company.condition());
        assertTrue(company.active());
        assertTrue(company.habido());
        assertTrue(company.valid());
        mockServer.verify();
    }

    @Test
    @DisplayName("validateRuc returns empty when RUC not found in SUNAT")
    void validateRuc_WhenNotFound_ReturnsEmpty() {
        String ruc = "10999999999";

        mockServer.expect(requestTo(API_URL + "/v1/ruc/info/" + ruc))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        var result = client.validateRuc(ruc);

        assertTrue(result.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("validateRuc returns empty without API call when format is invalid")
    void validateRuc_WhenMalformedFormat_ReturnsEmptyWithoutApiCall() {
        var result1 = client.validateRuc("12345");
        var result2 = client.validateRuc("nonnumeric1");
        var result3 = client.validateRuc(null);

        assertTrue(result1.isEmpty());
        assertTrue(result2.isEmpty());
        assertTrue(result3.isEmpty());
    }

    @Test
    @DisplayName("validateCe returns foreigner details when Migraciones record exists")
    void validateCe_WhenValidResponse_ReturnsPersonResult() {
        String ce = "000123456";
        String jsonResponse = """
                {
                  "status": 200,
                  "message": "Exito",
                  "success": true,
                  "data": {
                    "numero": "000123456",
                    "nombres": "JOHN",
                    "apellido_paterno": "DOE",
                    "apellido_materno": "SMITH",
                    "nombre_completo": "DOE SMITH, JOHN"
                  }
                }
                """;

        mockServer.expect(requestTo(API_URL + "/pe/v1/cee/info/" + ce))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", "Bearer " + API_TOKEN))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        var result = client.validateCe(ce);

        assertTrue(result.isPresent());
        var person = result.get();
        assertEquals("CE", person.documentType());
        assertEquals(ce, person.documentNumber());
        assertEquals("JOHN", person.firstName());
        assertEquals("DOE SMITH", person.lastName());
        assertTrue(person.valid());
        mockServer.verify();
    }

    @Test
    @DisplayName("validatePerson dispatches to DNI or CE based on type")
    void validatePerson_DispatchesCorrectly() {
        String dni = "72345678";
        String jsonResponse = """
                {
                  "status": 200,
                  "message": "Exito",
                  "success": true,
                  "data": {
                    "numero": "72345678",
                    "nombres": "JAZMIN",
                    "apellido_paterno": "PAUCAR",
                    "apellido_materno": "TTUPA"
                  }
                }
                """;

        mockServer.expect(requestTo(API_URL + "/v1/dni/info/" + dni))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        var result = client.validatePerson("dni", dni);

        assertTrue(result.isPresent());
        assertEquals("JAZMIN", result.get().firstName());
        mockServer.verify();
    }

    @Test
    @DisplayName("validateDni returns empty gracefully when server returns 500 error")
    void validateDni_WhenServerErrors_ReturnsEmpty() {
        String dni = "72345678";

        mockServer.expect(requestTo(API_URL + "/v1/dni/info/" + dni))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        var result = client.validateDni(dni);

        assertTrue(result.isEmpty());
        mockServer.verify();
    }

    @Test
    @DisplayName("validateDni returns simulated result when API token is empty")
    void validateDni_WhenTokenNotConfigured_ReturnsSimulatedResult() {
        RestClient.Builder builder = RestClient.builder();
        FactilizaClient unconfiguredClient = new FactilizaClient(API_URL, "", builder.build());

        var result = unconfiguredClient.validateDni("72345678");

        assertTrue(result.isPresent());
        assertTrue(result.get().valid());
    }
}
