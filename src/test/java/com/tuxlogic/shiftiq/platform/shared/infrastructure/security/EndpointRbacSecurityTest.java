package com.tuxlogic.shiftiq.platform.shared.infrastructure.security;

import com.tuxlogic.shiftiq.platform.billing.application.commandservices.QuoteCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.commandservices.VoucherCommandService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.QuoteQueryService;
import com.tuxlogic.shiftiq.platform.billing.application.queryservices.VoucherQueryService;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Voucher;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CancelQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.commands.CreateQuoteCommand;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetQuoteByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.domain.model.queries.GetVoucherByIdQuery;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.CheckoutsController;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.QuotesController;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.VouchersController;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.AddPaymentResource;
import com.tuxlogic.shiftiq.platform.billing.interfaces.rest.resources.ProcessCheckoutResource;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.BranchCommandService;
import com.tuxlogic.shiftiq.platform.core.application.commandservices.SubscriptionCommandService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.BranchesController;
import com.tuxlogic.shiftiq.platform.core.interfaces.rest.resources.UpdateBranchResource;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.EmployeeRegistration;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.queries.GetEmployeeRegistrationByIdQuery;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.AppointmentCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.commandservices.EmployeeRegistrationCommandService;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.AppointmentQueryService;
import com.tuxlogic.shiftiq.platform.fleet.application.queryservices.EmployeeRegistrationQueryService;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.AppointmentsController;
import com.tuxlogic.shiftiq.platform.fleet.interfaces.rest.EmployeeRegistrationsController;
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
import java.time.LocalDateTime;
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

    @Mock
    private BranchCommandService branchCommandService;

    @Mock
    private BranchQueryService branchQueryService;

    @Mock
    private SubscriptionCommandService subscriptionCommandService;

    @Mock
    private QuoteCommandService quoteCommandService;

    @Mock
    private QuoteQueryService quoteQueryService;

    @Mock
    private VoucherCommandService voucherCommandService;

    @Mock
    private VoucherQueryService voucherQueryService;

    @Mock
    private EmployeeRegistrationCommandService employeeRegistrationCommandService;

    @Mock
    private EmployeeRegistrationQueryService employeeRegistrationQueryService;

    @Mock
    private AppointmentCommandService appointmentCommandService;

    @Mock
    private AppointmentQueryService appointmentQueryService;

    private ProductsController productsController;
    private WorkOrdersController workOrdersController;
    private ServicesController servicesController;
    private MediaController mediaController;
    private BranchesController branchesController;
    private QuotesController quotesController;
    private VouchersController vouchersController;
    private CheckoutsController checkoutsController;
    private EmployeeRegistrationsController employeeRegistrationsController;
    private AppointmentsController appointmentsController;

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
        branchesController = new BranchesController(branchCommandService, branchQueryService, subscriptionCommandService, multiTenancySecurityService);
        quotesController = new QuotesController(quoteCommandService, quoteQueryService, messageSource, multiTenancySecurityService);
        vouchersController = new VouchersController(voucherCommandService, voucherQueryService, quoteQueryService, messageSource, multiTenancySecurityService);
        checkoutsController = new CheckoutsController(voucherCommandService, quoteQueryService, messageSource, multiTenancySecurityService);
        employeeRegistrationsController = new EmployeeRegistrationsController(employeeRegistrationCommandService, employeeRegistrationQueryService, messageSource, multiTenancySecurityService);
        appointmentsController = new AppointmentsController(appointmentCommandService, appointmentQueryService, messageSource, multiTenancySecurityService);
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

    @Test
    @DisplayName("BranchesController.updateBranch and cancelSubscription require manager access")
    void branchesControllerRequiresManagerAccess() {
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);

        assertThrows(AccessDeniedException.class,
                () -> branchesController.updateBranch(branchId, new UpdateBranchResource("BR-01", "Main", "Addr", "999888777")));
        assertThrows(AccessDeniedException.class,
                () -> branchesController.cancelSubscription(branchId));
        verify(branchCommandService, never()).handle(any(com.tuxlogic.shiftiq.platform.core.domain.model.commands.UpdateBranchCommand.class));
    }

    @Test
    @DisplayName("Billing controllers enforce advisor/manager for mutations and manager for quote cancellation")
    void billingControllersEnforceRoleHierarchy() {
        UUID quoteId = UUID.randomUUID();
        UUID voucherId = UUID.randomUUID();
        Quote quote = new Quote(new CreateQuoteCommand(workOrderId, new BranchId(branchId), 0.0), Money.of(100.0));

        when(quoteQueryService.handle(any(GetQuoteByIdQuery.class))).thenReturn(Optional.of(quote));

        // Mechanic denied on approveQuote, checkout, and addPayment
        doThrow(new AccessDeniedException("Unauthorized advisor or managerial access"))
                .when(multiTenancySecurityService).validateAdvisorOrManagerAccess(branchId);
        assertThrows(AccessDeniedException.class, () -> quotesController.approveQuote(quoteId));
        assertThrows(AccessDeniedException.class,
                () -> checkoutsController.checkout(new ProcessCheckoutResource(quoteId, "BOLETA", "DNI", "12345678", "Test", "CASH")));

        Voucher voucher = mock(Voucher.class);
        when(voucher.getQuoteId()).thenReturn(quoteId);
        when(voucherQueryService.handle(any(GetVoucherByIdQuery.class))).thenReturn(Optional.of(voucher));
        assertThrows(AccessDeniedException.class,
                () -> vouchersController.addPayment(voucherId, new AddPaymentResource(BigDecimal.valueOf(50.0), "CASH")));

        // Advisor denied on cancelQuote (manager required)
        doThrow(new AccessDeniedException("Unauthorized managerial access"))
                .when(multiTenancySecurityService).validateManagerAccess(branchId);
        assertThrows(AccessDeniedException.class, () -> quotesController.cancelQuote(quoteId));
        verify(quoteCommandService, never()).handle(any(CancelQuoteCommand.class));
    }

    @Test
    @DisplayName("Fleet controllers enforce manager on employee rejection and advisor/customer on appointment updates")
    void fleetControllersEnforceRoleHierarchy() {
        UUID regId = UUID.randomUUID();
        EmployeeRegistration reg = new EmployeeRegistration(
                UUID.randomUUID(), new BranchId(branchId), "MECANICO", "Desc", BigDecimal.valueOf(1500));
        when(employeeRegistrationQueryService.handle(any(GetEmployeeRegistrationByIdQuery.class)))
                .thenReturn(Result.success(reg));
        when(multiTenancySecurityService.isAuthorizedManagerForBranch(new BranchId(branchId))).thenReturn(false);

        ResponseEntity<?> rejectResp = employeeRegistrationsController.reject(regId, null);
        assertThat(rejectResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        UUID appointmentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        Appointment appointment = new Appointment(
                new BranchId(branchId), new CustomerId(customerId), new VehicleId(UUID.randomUUID()),
                LocalDateTime.now(), new AppointmentSummary("Service"));
        when(appointmentQueryService.handle(appointmentId)).thenReturn(Result.success(appointment));
        when(multiTenancySecurityService.isAuthorizedAdvisorOrManagerForBranch(new BranchId(branchId))).thenReturn(false);
        when(multiTenancySecurityService.isAuthorizedForSelf(customerId)).thenReturn(false);

        ResponseEntity<?> updateAppResp = appointmentsController.updateAppointment(appointmentId, null);
        assertThat(updateAppResp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
