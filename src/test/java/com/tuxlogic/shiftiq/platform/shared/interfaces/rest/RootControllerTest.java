package com.tuxlogic.shiftiq.platform.shared.interfaces.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RootControllerTest {

    private final RootController rootController = new RootController();

    @Test
    @DisplayName("root endpoint returns 200 OK with UP status and swagger link")
    void rootReturnsOk() {
        ResponseEntity<Map<String, String>> response = rootController.root();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
        assertThat(response.getBody().get("app")).isEqualTo("ShiftIQ Platform API");
        assertThat(response.getBody().get("docs")).isEqualTo("/swagger-ui.html");
    }

    @Test
    @DisplayName("health endpoint returns 200 OK with UP status")
    void healthReturnsOk() {
        ResponseEntity<Map<String, String>> response = rootController.health();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("UP");
    }
}
