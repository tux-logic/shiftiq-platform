package com.tuxlogic.shiftiq.platform.shared.infrastructure.outbound.factiliza;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FactilizaRucResponse(
        Integer status,
        Boolean success,
        String message,
        FactilizaRucData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FactilizaRucData(
            String numero,
            @JsonProperty("nombre_o_razon_social") String nombreORazonSocial,
            @JsonProperty("tipo_contribuyente") String tipoContribuyente,
            String estado,
            String condicion,
            String departamento,
            String provincia,
            String distrito,
            String direccion,
            @JsonProperty("direccion_completa") String direccionCompleta,
            @JsonProperty("ubigeo_sunat") String ubigeoSunat
    ) {}
}
