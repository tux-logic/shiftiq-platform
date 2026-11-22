package com.tuxlogic.shiftiq.platform.operations.infrastructure.outboundservices;

import com.tuxlogic.shiftiq.platform.operations.application.outboundservices.ExternalVehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter encapsulating vehicle summary query execution across the Operations-IoT boundary.
 */
@Service
public class ExternalVehicleServiceImpl implements ExternalVehicleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExternalVehicleServiceImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public ExternalVehicleServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<VehicleSummary> fetchVehicleSummary(UUID vehicleId) {
        if (vehicleId == null) {
            return Optional.empty();
        }
        try {
            VehicleSummary summary = jdbcTemplate.queryForObject(
                    "SELECT plate_number, brand, model, year FROM vehicles WHERE id = ? AND deleted_at IS NULL",
                    (rs, rowNum) -> new VehicleSummary(
                            rs.getString("plate_number"),
                            rs.getString("brand"),
                            rs.getString("model"),
                            rs.getObject("year", Integer.class)
                    ),
                    vehicleId
            );
            return Optional.ofNullable(summary);
        } catch (DataAccessException e) {
            LOGGER.warn("Failed to fetch vehicle summary for vehicleId: {}", vehicleId, e);
            return Optional.empty();
        }
    }
}
