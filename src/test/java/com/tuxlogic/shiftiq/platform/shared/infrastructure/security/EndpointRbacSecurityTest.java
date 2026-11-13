package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.inventory.application.commandservices.ProductCommandService;
import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.aggregates.Product;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.AddBatchToProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.commands.DeleteProductCommand;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.entities.ProductBatch;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.queries.GetProductByIdQuery;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.InventoryQuantity;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductCategory;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.ProductName;
import com.tuxlogic.shiftiq.platform.inventory.domain.model.valueobjects.Sku;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.ProductsController;
import com.tuxlogic.shiftiq.platform.inventory.interfaces.rest.resources.AddBatchToProductResource;
import com.tuxlogic.shiftiq.platform.operations.application.commandservices.ServiceCommandService;
import com.tuxlogic.shiftiq.platform.operations.application.commandservices.WorkOrderCommandService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.ServiceQueryService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.Service;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.DeleteWorkOrderCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.commands.UpdateServiceCommand;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetServiceByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.AppointmentId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.DiagnosticSummary;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.ServiceId;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderId;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.ServicesController;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.WorkOrdersController;
import com.tuxlogic.shiftiq.platform.operations.interfaces.rest.resources.UpdateServiceResource;
import com.tuxlogic.shiftiq.platform.shared.application.outboundservices.storage.StorageService;
import com.tuxlogic.shiftiq.platform.shared.application.result.Result;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Mileage;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import com.tuxlogic.shiftiq.platform.shared.interfaces.rest.MediaController;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndpointRbacSecurityTest {

    @Mock
    private MultiTenancySecurityService multiTenancySecurityService;

    @Mock
    private ProductCommandService productCommandService;

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private WorkOrderCommandService workOrderCommandService;

    @Mock
    private WorkOrderQueryService workOrderQueryService;

    @Mock
    private ServiceCommandService serviceCommandService;

    @Mock
    private ServiceQueryService serviceQueryService;

    @Mock
    private StorageService storageService;

    @Mock
    private MessageSource messageSource;

    private ProductsController productsController;
    private WorkOrdersController workOrdersController;
    private ServicesController servicesController;
    private MediaController mediaController;

    private final UUID branchId = UUID.randomUUID();
    private final UUID productId = UUID.randomUUID();
    private final UUID workOrderId = UUID.randomUUID();
    private final UUID serviceId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        productsController = new ProductsController(productCommandService, productQueryService, multiTenancySecurityService, messageSource);
        workOrdersController = new WorkOrdersController(workOrderCommandService, workOrderQueryService, multiTenancySecurityService, messageSource);
        servicesController = new ServicesController(serviceCommandService, serviceQueryService, multiTenancySecurityService);
        mediaController = new MediaController(storageService);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticate(boolean isStaff) {
        var principal = Mockito.mock(AuthenticatedPrincipal.class);
        Mockito.lenient().when(principal.getId()).thenReturn(UUID.randomUUID());
        Mockito.lenient().when(principal.hasRole("ROLE_ADMIN")).thenReturn(isStaff);
        Mockito.lenient().when(principal.hasRole("ROLE_OWNER")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_BRANCH_MANAGER")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_EMPLOYEE")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_ASSISTANT")).thenReturn(false);
        Mockito.lenient().when(principal.hasRole("ROLE_USER")).thenReturn(!isStaff);
        Mockito.lenient().when(principal.getBranchIds()).thenReturn(Set.of(branchId));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of())
        );
    }

    private Product sampleProduct() {
        return new Product(
                productId,
                new BranchId(branchId),
                new ProductCategory("PARTS"),
                new ProductName("Brake Pads"),
                new Sku("SKU-BP-1"),
                new Money(BigDecimal.valueOf(30)),
                "Quality brake pads",
                5
        );
    }

    @Test
    @DisplayName("ProductsController.deleteProduct allows branch manager and returns 204")
    void deleteProductAllowsManager() {
        Product product = sampleProduct();
        when(productQueryService.handle(any(GetProductByIdQuery.class))).thenReturn(Optional.of(product));
        when(productCommandService.handle(any(DeleteProductCommand.class))).thenReturn(Result.success(null));

        ResponseEntity<?> response = productsController.deleteProduct(productId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(multiTenancySecurityService).validateManagerAccess(branchId);
    }

    @Test
    @DisplayName("ProductsController.deleteProduct throws AccessDeniedException when manager access denied")
    void deleteProductDeniesNonManager() {
        Product product = sampleProduct();
        when(productQueryService.handle(any(GetProductByIdQuery.class))).thenReturn(Optional.of(product));
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);

        assertThrows(AccessDeniedException.class, () -> productsController.deleteProduct(productId));
        verify(productCommandService, never()).handle(any(DeleteProductCommand.class));
    }

    @Test
    @DisplayName("ProductsController.addBatchToProduct allows branch manager and returns 201")
    void addBatchToProductAllowsManager() {
        Product product = sampleProduct();
        ProductBatch batch = new ProductBatch(UUID.randomUUID(), new InventoryQuantity(10), new Money(BigDecimal.valueOf(25.5)));
        when(productQueryService.handle(any(GetProductByIdQuery.class))).thenReturn(Optional.of(product));
        when(productCommandService.handle(any(AddBatchToProductCommand.class))).thenReturn(Result.success(batch));

        AddBatchToProductResource resource = new AddBatchToProductResource(10, 25.5);
        ResponseEntity<?> response = productsController.addBatchToProduct(productId, resource);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(multiTenancySecurityService).validateManagerAccess(branchId);
    }

    @Test
    @DisplayName("ProductsController.addBatchToProduct throws AccessDeniedException when manager access denied")
    void addBatchToProductDeniesNonManager() {
        Product product = sampleProduct();
        when(productQueryService.handle(any(GetProductByIdQuery.class))).thenReturn(Optional.of(product));
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);

        AddBatchToProductResource resource = new AddBatchToProductResource(10, 25.5);
        assertThrows(AccessDeniedException.class, () -> productsController.addBatchToProduct(productId, resource));
        verify(productCommandService, never()).handle(any(AddBatchToProductCommand.class));
    }

    @Test
    @DisplayName("WorkOrdersController.deleteWorkOrder allows branch manager and returns 204")
    void deleteWorkOrderAllowsManager() {
        WorkOrder workOrder = new WorkOrder(
                new AppointmentId(UUID.randomUUID()),
                new BranchId(branchId),
                new VehicleId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                101,
                new DiagnosticSummary("Initial inspection"),
                new Mileage(60000)
        );
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(workOrder));
        when(workOrderCommandService.handle(any(DeleteWorkOrderCommand.class))).thenReturn(Result.success(workOrder));

        ResponseEntity<?> response = workOrdersController.deleteWorkOrder(workOrderId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(multiTenancySecurityService).validateManagerAccess(branchId);
    }

    @Test
    @DisplayName("WorkOrdersController.deleteWorkOrder throws AccessDeniedException when manager access denied")
    void deleteWorkOrderDeniesNonManager() {
        WorkOrder workOrder = new WorkOrder(
                new AppointmentId(UUID.randomUUID()),
                new BranchId(branchId),
                new VehicleId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                101,
                new DiagnosticSummary("Initial inspection"),
                new Mileage(60000)
        );
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(workOrder));
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);

        assertThrows(AccessDeniedException.class, () -> workOrdersController.deleteWorkOrder(workOrderId));
        verify(workOrderCommandService, never()).handle(any(DeleteWorkOrderCommand.class));
    }

    @Test
    @DisplayName("ServicesController.updateService allows manager and returns 200")
    void updateServiceAllowsManager() {
        Service service = new Service(new BranchId(branchId), "Oil Change", new Money(BigDecimal.valueOf(50)));
        when(serviceQueryService.handle(any(GetServiceByIdQuery.class))).thenReturn(Optional.of(service));
        when(serviceCommandService.handle(any(UpdateServiceCommand.class))).thenReturn(Result.success(service));

        UpdateServiceResource resource = new UpdateServiceResource("Oil Change Premium", 65.0);
        ResponseEntity<?> response = servicesController.updateService(serviceId, resource);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(multiTenancySecurityService).validateManagerAccess(branchId);
    }

    @Test
    @DisplayName("ServicesController.updateService throws AccessDeniedException when manager access denied")
    void updateServiceDeniesNonManager() {
        Service service = new Service(new BranchId(branchId), "Oil Change", new Money(BigDecimal.valueOf(50)));
        when(serviceQueryService.handle(any(GetServiceByIdQuery.class))).thenReturn(Optional.of(service));
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);

        UpdateServiceResource resource = new UpdateServiceResource("Oil Change Premium", 65.0);
        assertThrows(AccessDeniedException.class, () -> servicesController.updateService(serviceId, resource));
        verify(serviceCommandService, never()).handle(any(UpdateServiceCommand.class));
    }

    @Test
    @DisplayName("ServicesController.deleteService allows manager and returns 204")
    void deleteServiceAllowsManager() {
        Service service = new Service(new BranchId(branchId), "Oil Change", new Money(BigDecimal.valueOf(50)));
        when(serviceQueryService.handle(any(GetServiceByIdQuery.class))).thenReturn(Optional.of(service));
        when(serviceCommandService.handle(any(DeleteServiceCommand.class))).thenReturn(Result.success(null));

        ResponseEntity<?> response = servicesController.deleteService(serviceId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(multiTenancySecurityService).validateManagerAccess(branchId);
    }

    @Test
    @DisplayName("ServicesController.deleteService throws AccessDeniedException when manager access denied")
    void deleteServiceDeniesNonManager() {
        Service service = new Service(new BranchId(branchId), "Oil Change", new Money(BigDecimal.valueOf(50)));
        when(serviceQueryService.handle(any(GetServiceByIdQuery.class))).thenReturn(Optional.of(service));
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);

        assertThrows(AccessDeniedException.class, () -> servicesController.deleteService(serviceId));
        verify(serviceCommandService, never()).handle(any(DeleteServiceCommand.class));
    }

    @Test
    @DisplayName("MediaController.uploadMedia enforces folder whitelist and staff role")
    void mediaUploadEnforcesRbacAndWhitelist() {
        MockMultipartFile file = new MockMultipartFile("file", "image.png", "image/png", "bytes".getBytes());

        // Disallowed folder -> 400 Bad Request
        authenticate(true);
        ResponseEntity<?> badFolderResp = mediaController.uploadMedia(file, "malicious_folder");
        assertThat(badFolderResp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // Customer to products folder -> 403 Forbidden
        authenticate(false);
        ResponseEntity<?> customerResp = mediaController.uploadMedia(file, "products");
        assertThat(customerResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Staff to products folder -> 201 Created
        authenticate(true);
        when(storageService.uploadFile(any(), eq("products"))).thenReturn("https://cloudinary.com/shiftiq/products/img.png");
        ResponseEntity<?> staffResp = mediaController.uploadMedia(file, "products");
        assertThat(staffResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }
}
