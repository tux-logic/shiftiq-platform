package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchCommandService;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.SubscriptionCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchCommand;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateBranchResource;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.infrastructure.security.MultiTenancySecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BranchesControllerTest {

    @Mock
    private BranchCommandService branchCommandService;

    @Mock
    private BranchQueryService branchQueryService;

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    private BranchesController controller;

    @BeforeEach
    void setUp() {
        controller = new BranchesController(branchCommandService, branchQueryService, subscriptionCommandService, multiTenancySecurityService);
    }

    @Test
    void createBranch_WhenWorkshopAccessDenied_ShouldThrowException() {
        UUID workshopId = UUID.randomUUID();
        CreateBranchResource resource = new CreateBranchResource(workshopId, "BR-01", "Branch Principal", "Av. Peru 123", "999888777");

        doThrow(new AccessDeniedException("Unauthorized access"))
                .when(multiTenancySecurityService).validateWorkshopAccess(workshopId);

        assertThrows(AccessDeniedException.class, () -> controller.createBranch(resource));
    }

    @Test
    void createBranch_WhenValid_ShouldReturnCreated() {
        UUID workshopId = UUID.randomUUID();
        CreateBranchResource resource = new CreateBranchResource(workshopId, "BR-01", "Branch Principal", "Av. Peru 123", "999888777");
        Branch branch = mock(Branch.class);
        when(branch.getId()).thenReturn(new BranchId(UUID.randomUUID()));

        when(branchCommandService.handle(any(CreateBranchCommand.class))).thenReturn(Optional.of(branch));

        ResponseEntity<?> response = controller.createBranch(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        verify(multiTenancySecurityService).validateWorkshopAccess(workshopId);
    }
}
