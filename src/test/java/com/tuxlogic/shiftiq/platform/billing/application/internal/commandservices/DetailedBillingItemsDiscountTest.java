package com.tuxlogic.shiftiq.platform.billing.application.internal.commandservices;

import com.tuxlogic.shiftiq.platform.billing.application.outboundservices.FactosGateway;
import com.tuxlogic.shiftiq.platform.billing.domain.model.aggregates.Quote;
import com.tuxlogic.shiftiq.platform.billing.domain.model.valueobjects.QuoteStatus;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.QuoteRepository;
import com.tuxlogic.shiftiq.platform.billing.domain.repositories.VoucherRepository;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.BranchQueryService;
import com.tuxlogic.shiftiq.platform.core.application.queryservices.WorkshopQueryService;
import com.tuxlogic.shiftiq.platform.inventory.application.queryservices.ProductQueryService;
import com.tuxlogic.shiftiq.platform.operations.application.queryservices.WorkOrderQueryService;
import com.tuxlogic.shiftiq.platform.operations.domain.model.aggregates.WorkOrder;
import com.tuxlogic.shiftiq.platform.operations.domain.model.entities.WorkOrderTask;
import com.tuxlogic.shiftiq.platform.operations.domain.model.queries.GetWorkOrderByIdQuery;
import com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.TaskDescription;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DetailedBillingItemsDiscountTest {

    @Mock
    private VoucherRepository voucherRepository;
    @Mock
    private QuoteRepository quoteRepository;
    @Mock
    private BranchQueryService branchQueryService;
    @Mock
    private WorkshopQueryService workshopQueryService;
    @Mock
    private FactosGateway factosGateway;
    @Mock
    private WorkOrderQueryService workOrderQueryService;
    @Mock
    private ProductQueryService productQueryService;
    @Mock
    private VoucherExecutionService voucherExecutionService;

    private VoucherCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new VoucherCommandServiceImpl(
                voucherRepository,
                quoteRepository,
                branchQueryService,
                workshopQueryService,
                factosGateway,
                workOrderQueryService,
                productQueryService,
                voucherExecutionService,
                null
        );
    }

    @Test
    @DisplayName("getDetailedBillingItems prorates discount and matches quote total amount exactly")
    void getDetailedBillingItemsProratesDiscountCorrectly() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        // Subtotal = 200.00, Discount = 10%, Total = 180.00
        BigDecimal subtotal = new BigDecimal("200.00");
        BigDecimal total = new BigDecimal("180.00");
        Quote quote = new Quote(quoteId, workOrderId, new BranchId(branchId), new Money(subtotal), 10.0, new Money(total), QuoteStatus.APPROVED);

        WorkOrder mockWorkOrder = mock(WorkOrder.class);
        WorkOrderTask task1 = mock(WorkOrderTask.class);
        when(task1.getId()).thenReturn(new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId(UUID.randomUUID()));
        when(task1.getDescription()).thenReturn(new TaskDescription("Cambio de aceite"));
        when(task1.getPrice()).thenReturn(Money.of(120.00));
        when(task1.getProducts()).thenReturn(List.of());

        WorkOrderTask task2 = mock(WorkOrderTask.class);
        when(task2.getId()).thenReturn(new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId(UUID.randomUUID()));
        when(task2.getDescription()).thenReturn(new TaskDescription("Revisión de frenos"));
        when(task2.getPrice()).thenReturn(Money.of(80.00));
        when(task2.getProducts()).thenReturn(List.of());

        when(mockWorkOrder.getTasks()).thenReturn(List.of(task1, task2));
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(mockWorkOrder));

        List<FactosGateway.FactosItem> items = service.getDetailedBillingItems(quote);

        assertThat(items).hasSize(2);
        BigDecimal sum = items.stream()
                .map(i -> i.unitPrice().multiply(i.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(sum).isEqualByComparingTo(total);
        assertThat(items.get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("108.00")); // 120 * 0.9
        assertThat(items.get(1).unitPrice()).isEqualByComparingTo(new BigDecimal("72.00"));  // 80 * 0.9
    }

    @Test
    @DisplayName("getDetailedBillingItems without discount preserves original unit prices")
    void getDetailedBillingItemsWithoutDiscountPreservesPrices() {
        UUID workOrderId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();

        BigDecimal subtotal = new BigDecimal("100.00");
        Quote quote = new Quote(quoteId, workOrderId, new BranchId(branchId), new Money(subtotal), 0.0, new Money(subtotal), QuoteStatus.APPROVED);

        WorkOrder mockWorkOrder = mock(WorkOrder.class);
        WorkOrderTask task = mock(WorkOrderTask.class);
        when(task.getId()).thenReturn(new com.tuxlogic.shiftiq.platform.operations.domain.model.valueobjects.WorkOrderTaskId(UUID.randomUUID()));
        when(task.getDescription()).thenReturn(new TaskDescription("Alineación"));
        when(task.getPrice()).thenReturn(Money.of(100.00));
        when(task.getProducts()).thenReturn(List.of());

        when(mockWorkOrder.getTasks()).thenReturn(List.of(task));
        when(workOrderQueryService.handle(any(GetWorkOrderByIdQuery.class))).thenReturn(Optional.of(mockWorkOrder));

        List<FactosGateway.FactosItem> items = service.getDetailedBillingItems(quote);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).unitPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
}
