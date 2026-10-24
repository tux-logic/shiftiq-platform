package com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FactilizaCeResponse(
        Integer status,
        Boolean success,
        String message,
        FactilizaCeData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FactilizaCeData(
            String numero,
            String nombres,
            @JsonProperty("apellido_paterno") String apellidoPaterno,
            @JsonProperty("apellido_materno") String apellidoMaterno,
            @JsonProperty("nombre_completo") String nombreCompleto
    ) {}
}
