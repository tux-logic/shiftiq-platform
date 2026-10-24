package com.tuxlogic.shiftiq.platform.shared.application.outboundservices.identity;

import java.util.Optional;

/**
 * Outbound port for validating Peruvian identity documents (DNI, RUC, CE) and retrieving official registry data.
 */
public interface IdentityValidationService {

    /**
     * Queries and validates an 8-digit DNI number via RENIEC.
     *
     * @param dni the 8-digit DNI number
     * @return optional containing the person identity result if found
     */
    Optional<PersonIdentityResult> validateDni(String dni);

    /**
     * Queries and validates a Carnet de Extranjería (CE / CEE) number via Migraciones.
     *
     * @param ce the foreigner identity document number
     * @return optional containing the foreigner identity result if found
     */
    Optional<PersonIdentityResult> validateCe(String ce);

    /**
     * Queries and validates an 11-digit RUC number via SUNAT.
     *
     * @param ruc the 11-digit RUC number
     * @return optional containing the company fiscal result if found
     */
    Optional<CompanyIdentityResult> validateRuc(String ruc);

    /**
     * Dispatches lookup according to document type (DNI, CE, or RUC).
     *
     * @param documentType the document type ("DNI", "CE", or "RUC")
     * @param documentNumber the document number
     * @return optional person result if DNI/CE, or empty
     */
    Optional<PersonIdentityResult> validatePerson(String documentType, String documentNumber);
}
