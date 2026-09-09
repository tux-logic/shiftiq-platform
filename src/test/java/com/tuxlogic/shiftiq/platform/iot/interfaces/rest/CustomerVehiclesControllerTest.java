package com.tuxlogic.shiftiq.platform.iot.interfaces.rest;

import com.tuxlogic.shiftiq.platform.iot.application.queryservices.VehicleQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetActiveVehiclesByCustomerIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.services.CustomerDirectoryPort;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.UserSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerVehiclesControllerTest {

    @Mock
    private VehicleQueryService vehicleQueryService;

    @Mock
    private CustomerDirectoryPort customerDirectoryPort;

    @Mock
    private UserSecurityService userSecurityService;

    private CustomerVehiclesController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerVehiclesController(vehicleQueryService, customerDirectoryPort, userSecurityService);
    }

    @Test
    void getActiveVehiclesByCustomerId_WhenForbidden_ShouldReturnForbiddenStatus() {
        UUID customerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(customerDirectoryPort.findUserIdByCustomerId(customerId)).thenReturn(Optional.of(userId));
        when(userSecurityService.isCurrentUser(userId)).thenReturn(false);

        ResponseEntity<?> response = controller.getActiveVehiclesByCustomerId(customerId);

        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getActiveVehiclesByCustomerId_WhenAuthorized_ShouldReturnOk() {
        UUID customerId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(customerDirectoryPort.findUserIdByCustomerId(customerId)).thenReturn(Optional.of(userId));
        when(userSecurityService.isCurrentUser(userId)).thenReturn(true);
        when(vehicleQueryService.handle(any(GetActiveVehiclesByCustomerIdQuery.class))).thenReturn(List.of());

        ResponseEntity<?> response = controller.getActiveVehiclesByCustomerId(customerId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
