package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalEmployeeServiceImplTest {

    @Mock
    private JdbcTemplate jdbcTemplate;

    private ExternalEmployeeServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ExternalEmployeeServiceImpl(jdbcTemplate);
    }

    @Test
    void resolveActiveRegistrationId_WhenCandidateIdIsRegistrationId_ShouldReturnCandidateId() {
        UUID registrationId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(registrationId), eq(branchId)))
                .thenReturn(List.of(registrationId));

        UUID result = service.resolveActiveRegistrationId(registrationId, branchId);
        assertEquals(registrationId, result);
    }

    @Test
    void resolveActiveRegistrationId_WhenCandidateIdIsEmployeeProfileId_ShouldReturnRegistrationId() {
        UUID employeeId = UUID.randomUUID();
        UUID registrationId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(employeeId), eq(branchId)))
                .thenReturn(Collections.emptyList()) // 1st query by id fails
                .thenReturn(List.of(registrationId)); // 2nd query by employee_id succeeds

        UUID result = service.resolveActiveRegistrationId(employeeId, branchId);
        assertEquals(registrationId, result);
    }

    @Test
    void resolveActiveRegistrationId_WhenNotFound_ShouldReturnCandidateIdFallback() {
        UUID candidateId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(candidateId), eq(branchId)))
                .thenReturn(Collections.emptyList())
                .thenReturn(Collections.emptyList());

        UUID result = service.resolveActiveRegistrationId(candidateId, branchId);
        assertEquals(candidateId, result);
    }
}

