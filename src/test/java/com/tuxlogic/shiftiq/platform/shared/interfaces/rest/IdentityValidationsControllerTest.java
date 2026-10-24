package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.CompanyIdentityResult;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.PersonIdentityResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class IdentityValidationsControllerTest {

    @Mock
    private IdentityValidationService identityValidationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var controller = new IdentityValidationsController(identityValidationService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("GET /lookup with valid DNI returns 200 OK and person details")
    void lookup_WhenValidDni_ReturnsOkWithPersonData() throws Exception {
        var person = new PersonIdentityResult(
                "DNI", "72345678", "JAZMIN", "PAUCAR TTUPA", "PAUCAR TTUPA, JAZMIN",
                "COLCA - YANATILE", "CUSCO", "CALCA", "YANATILE", "4", true
        );

        when(identityValidationService.validateDni("72345678")).thenReturn(Optional.of(person));

        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "DNI")
                        .param("number", "72345678")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentType").value("DNI"))
                .andExpect(jsonPath("$.documentNumber").value("72345678"))
                .andExpect(jsonPath("$.firstName").value("JAZMIN"))
                .andExpect(jsonPath("$.lastName").value("PAUCAR TTUPA"))
                .andExpect(jsonPath("$.valid").value(true));

        verify(identityValidationService, times(1)).validateDni("72345678");
    }

    @Test
    @DisplayName("GET /lookup with valid RUC returns 200 OK and company details")
    void lookup_WhenValidRuc_ReturnsOkWithCompanyData() throws Exception {
        var company = new CompanyIdentityResult(
                "20552103816", "AGROLIGHT PERU S.A.C.", "ACTIVO", "HABIDO",
                "PJ. JORGE BASADRE NRO. 158", "LIMA", "LIMA", "SANTA ANITA", "150137",
                true, true, true
        );

        when(identityValidationService.validateRuc("20552103816")).thenReturn(Optional.of(company));

        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "RUC")
                        .param("number", "20552103816")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentType").value("RUC"))
                .andExpect(jsonPath("$.documentNumber").value("20552103816"))
                .andExpect(jsonPath("$.businessName").value("AGROLIGHT PERU S.A.C."))
                .andExpect(jsonPath("$.status").value("ACTIVO"))
                .andExpect(jsonPath("$.condition").value("HABIDO"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.habido").value(true));

        verify(identityValidationService, times(1)).validateRuc("20552103816");
    }

    @Test
    @DisplayName("GET /lookup with valid CE returns 200 OK and foreigner details")
    void lookup_WhenValidCe_ReturnsOkWithForeignerData() throws Exception {
        var foreigner = new PersonIdentityResult(
                "CE", "000123456", "JOHN", "DOE SMITH", "DOE SMITH, JOHN",
                null, null, null, null, null, true
        );

        when(identityValidationService.validateCe("000123456")).thenReturn(Optional.of(foreigner));

        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "CE")
                        .param("number", "000123456")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documentType").value("CE"))
                .andExpect(jsonPath("$.documentNumber").value("000123456"))
                .andExpect(jsonPath("$.firstName").value("JOHN"))
                .andExpect(jsonPath("$.lastName").value("DOE SMITH"));

        verify(identityValidationService, times(1)).validateCe("000123456");
    }

    @Test
    @DisplayName("GET /lookup returns 404 when document not found in official registry")
    void lookup_WhenDocumentNotFound_ReturnsNotFound() throws Exception {
        when(identityValidationService.validateDni("99999999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "DNI")
                        .param("number", "99999999")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(identityValidationService, times(1)).validateDni("99999999");
    }

    @Test
    @DisplayName("GET /lookup with invalid DNI format returns 400 Bad Request")
    void lookup_WhenInvalidDniFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "DNI")
                        .param("number", "123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(identityValidationService);
    }

    @Test
    @DisplayName("GET /lookup with invalid RUC format returns 400 Bad Request")
    void lookup_WhenInvalidRucFormat_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "RUC")
                        .param("number", "12345")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(identityValidationService);
    }

    @Test
    @DisplayName("GET /lookup with unsupported type returns 400 Bad Request")
    void lookup_WhenUnsupportedType_ReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/identity-validations/lookup")
                        .param("type", "PASSPORT")
                        .param("number", "A1234567")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(identityValidationService);
    }
}
