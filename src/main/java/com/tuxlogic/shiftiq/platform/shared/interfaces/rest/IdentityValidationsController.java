package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity.IdentityValidationService;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.resources.IdentityLookupResource;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.transform.IdentityLookupResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/identity-validations", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Identity Validations", description = "Official Peruvian Identity Document Validation Endpoints (Factiliza / RENIEC / SUNAT / Migraciones)")
public class IdentityValidationsController {

    private final IdentityValidationService identityValidationService;

    public IdentityValidationsController(IdentityValidationService identityValidationService) {
        this.identityValidationService = identityValidationService;
    }

    @GetMapping("/lookup")
    @Operation(
            summary = "Validate and lookup identity document",
            description = "Queries RENIEC (DNI), SUNAT (RUC), or Migraciones (CE) via Factiliza and returns official registry details for form auto-completion"
    )
    public ResponseEntity<IdentityLookupResource> lookup(
            @Parameter(description = "Document type: DNI, RUC, or CE", required = true)
            @RequestParam("type") String type,
            @Parameter(description = "Document number (8 digits for DNI, 11 digits for RUC, or 7-12 chars for CE)", required = true)
            @RequestParam("number") String number
    ) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("shared.error.identity.typeRequired");
        }
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("shared.error.identity.numberRequired");
        }

        String normalizedType = type.trim().toUpperCase();
        String normalizedNumber = number.trim();

        return switch (normalizedType) {
            case "DNI" -> {
                if (!normalizedNumber.matches("^[0-9]{8}$")) {
                    throw new IllegalArgumentException("shared.error.identity.invalidNumber");
                }
                var person = identityValidationService.validateDni(normalizedNumber);
                yield person.map(p -> ResponseEntity.ok(IdentityLookupResourceFromEntityAssembler.toResourceFromPerson(p)))
                        .orElseGet(() -> ResponseEntity.notFound().build());
            }
            case "RUC" -> {
                if (!normalizedNumber.matches("^[0-9]{11}$")) {
                    throw new IllegalArgumentException("shared.error.identity.invalidNumber");
                }
                var company = identityValidationService.validateRuc(normalizedNumber);
                yield company.map(c -> ResponseEntity.ok(IdentityLookupResourceFromEntityAssembler.toResourceFromCompany(c)))
                        .orElseGet(() -> ResponseEntity.notFound().build());
            }
            case "CE", "CEE" -> {
                if (normalizedNumber.length() < 7 || normalizedNumber.length() > 12) {
                    throw new IllegalArgumentException("shared.error.identity.invalidNumber");
                }
                var person = identityValidationService.validateCe(normalizedNumber);
                yield person.map(p -> ResponseEntity.ok(IdentityLookupResourceFromEntityAssembler.toResourceFromPerson(p)))
                        .orElseGet(() -> ResponseEntity.notFound().build());
            }
            default -> throw new IllegalArgumentException("shared.error.identity.invalidType");
        };
    }
}
