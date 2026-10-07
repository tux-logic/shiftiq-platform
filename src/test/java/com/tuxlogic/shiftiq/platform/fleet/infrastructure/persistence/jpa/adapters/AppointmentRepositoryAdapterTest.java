package com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.adapters;

import com.tuxlogic.shiftiq.platform.fleet.domain.model.aggregates.Appointment;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.events.AppointmentCreatedEvent;
import com.tuxlogic.shiftiq.platform.fleet.domain.model.valueobjects.AppointmentSummary;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.entities.AppointmentPersistenceEntity;
import com.tuxlogic.shiftiq.platform.fleet.infrastructure.persistence.jpa.repositories.AppointmentJpaRepository;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.BranchId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.CustomerId;
import com.tuxlogic.shiftiq.platform.shared.domain.model.valueobjects.VehicleId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppointmentRepositoryAdapterTest {

    @Mock
    private AppointmentJpaRepository appointmentJpaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AppointmentRepositoryAdapter adapter;

    @Test
    @DisplayName("save publishes AppointmentCreatedEvent and clears the aggregate domain events")
    void savePublishesAppointmentCreatedEventAndClearsDomainEvents() {
        var appointment = new Appointment(
                new BranchId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                new VehicleId(UUID.randomUUID()),
                LocalDateTime.now().plusDays(1),
                new AppointmentSummary("Cita inicial")
        );
        when(appointmentJpaRepository.findById(any())).thenReturn(Optional.empty());
        when(appointmentJpaRepository.save(any(AppointmentPersistenceEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        adapter.save(appointment);

        verify(eventPublisher).publishEvent(any(AppointmentCreatedEvent.class));
        assertThat(appointment.domainEvents()).isEmpty();
    }
}
