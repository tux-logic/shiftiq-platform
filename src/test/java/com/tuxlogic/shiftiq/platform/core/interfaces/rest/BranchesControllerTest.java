package com.tuxlogic.shiftiq.platform.core.interfaces.rest;

import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchCommandService;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.SubscriptionCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.domain.model.aggregates.Branch;
import com.tuxlogic.shiftiq.platform.core.domain.model.commands.CreateBranchCommand;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.CreateBranchResource;
import com.tuxlogic.shiftiq.platform.iam.domain.model.valueobjects.Permission;
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
                .when(multiTenancySecurityService).validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);

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
        verify(multiTenancySecurityService).validateWorkshopPermission(workshopId, Permission.BRANCH_MANAGE);
    }

    @Test
    void createBranch_WhenCoverImageUrlProvided_ShouldIncludeInResource() {
        UUID workshopId = UUID.randomUUID();
        String coverUrl = "https://res.cloudinary.com/demo/image/upload/v1/shiftiq/branches/cover.jpg";
        CreateBranchResource resource = new CreateBranchResource(workshopId, "BR-02", "Branch Norte", "Av. Norte 456", "999111222", coverUrl);
        Branch branch = mock(Branch.class);
        when(branch.getId()).thenReturn(new BranchId(UUID.randomUUID()));
        when(branch.getCoverImageUrl()).thenReturn(coverUrl);

        when(branchCommandService.handle(any(CreateBranchCommand.class))).thenReturn(Optional.of(branch));

        ResponseEntity<?> response = controller.createBranch(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var body = (com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.BranchResource) response.getBody();
        assertNotNull(body);
        assertEquals(coverUrl, body.coverImageUrl());
    }

    @Test
    void createBranch_WhenCoordinatesProvided_ShouldIncludeInResource() {
        UUID workshopId = UUID.randomUUID();
        Double lat = -12.111062;
        Double lng = -76.991944;
        CreateBranchResource resource = new CreateBranchResource(
                workshopId, "BR-03", "Branch Surco", "Av. Primavera 1234", "999222333", null, lat, lng);
        Branch branch = mock(Branch.class);
        when(branch.getId()).thenReturn(new BranchId(UUID.randomUUID()));
        when(branch.getLatitude()).thenReturn(lat);
        when(branch.getLongitude()).thenReturn(lng);

        when(branchCommandService.handle(any(CreateBranchCommand.class))).thenReturn(Optional.of(branch));

        ResponseEntity<?> response = controller.createBranch(resource);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        var body = (com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.BranchResource) response.getBody();
        assertNotNull(body);
        assertEquals(lat, body.latitude());
        assertEquals(lng, body.longitude());
    }

    @Test
    void branchDomain_WhenCoordinatesIncompleteOrOutOfRange_ShouldThrow() {
        var workshopId = new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.WorkshopId(UUID.randomUUID());
        var addr = new com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Address("Av. Lima 123");
        var phone = new com.tuxlogic.shiftiq.platform.core.domain.model.valueobjects.Phone("+51999888777");

        assertThrows(IllegalArgumentException.class,
                () -> new Branch(workshopId, "BR-X", "Test", addr, phone, null, -12.0, null));
        assertThrows(IllegalArgumentException.class,
                () -> new Branch(workshopId, "BR-X", "Test", addr, phone, null, 95.0, -77.0));
        assertThrows(IllegalArgumentException.class,
                () -> new Branch(workshopId, "BR-X", "Test", addr, phone, null, -12.0, 190.0));
    }

    @Test
    void getBranchCatalog_WhenCustomerScoped_ShouldFilterByRegisteredWorkshopsAndIncludeBanner() {
        var ownWorkshopId = UUID.randomUUID();
        var otherWorkshopId = UUID.randomUUID();
        var item1 = new com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchCatalogItem(
                UUID.randomUUID(), ownWorkshopId, "AT Motors", "AT MOTORS EIRL", "MIR-01",
                "Sede Miraflores", "Jr. Toribio Pacheco 550", "+5114451234",
                "https://cdn.example.com/banner.jpg", -12.1128, -77.0454, 4.8, 10, 4, 2);
        var item2 = new com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchCatalogItem(
                UUID.randomUUID(), otherWorkshopId, "Competitor", "COMPETITOR SAC", "SUR-01",
                "Sede Surco", "Av. Caminos del Inca 100", "+5114459999",
                null, -12.13, -76.99, 4.0, 2, 2, 1);

        when(multiTenancySecurityService.resolveCustomerWorkshopScope())
                .thenReturn(Optional.of(java.util.List.of(ownWorkshopId)));
        when(branchQueryService.findCatalog()).thenReturn(java.util.List.of(item1, item2));

        var response = controller.getBranchCatalog();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(ownWorkshopId, response.getBody().get(0).workshopId());
        assertEquals("https://cdn.example.com/banner.jpg", response.getBody().get(0).coverImageUrl());
        assertEquals(-12.1128, response.getBody().get(0).latitude());
        assertEquals(-77.0454, response.getBody().get(0).longitude());
    }
}
