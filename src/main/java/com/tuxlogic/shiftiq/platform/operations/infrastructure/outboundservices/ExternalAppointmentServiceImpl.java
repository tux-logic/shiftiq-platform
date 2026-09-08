package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalAppointmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * ACL adapter for checking appointment existence in fleet context via database queries.
 */
@Service
public class ExternalAppointmentServiceImpl implements ExternalAppointmentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalAppointmentServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ExternalAppointmentServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean existsAppointment(UUID appointmentId) {
        if (appointmentId == null) {
            return false;
        }
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM appointments WHERE id = ?", Integer.class, appointmentId);
            return count != null && count > 0;
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to verify appointment existence for id: {}", appointmentId, e);
            return false;
        }
    }
}
