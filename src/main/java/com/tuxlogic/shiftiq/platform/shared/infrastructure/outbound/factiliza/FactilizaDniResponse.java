package com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FactilizaDniResponse(
        Integer status,
        Boolean success,
        String message,
        FactilizaDniData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FactilizaDniData(
            String numero,
            @JsonProperty("codigo_verificacion") String codigoVerificacion,
            String nombres,
            @JsonProperty("apellido_paterno") String apellidoPaterno,
            @JsonProperty("apellido_materno") String apellidoMaterno,
            @JsonProperty("nombre_completo") String nombreCompleto,
            String departamento,
            String provincia,
            String distrito,
            String direccion,
            @JsonProperty("direccion_completa") String direccionCompleta
    ) {}
}
