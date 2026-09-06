package com.tuxlogic.shiftiq.platform.iot.domain.repositories;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.Obd2DeviceRegistrationId;

import java.time.Instant;
import java.util.List;

/**
 * Domain repository interface/port for DtcAlert aggregates.
 */
public interface DtcAlertRepository {

    /**
     * Saves a single DtcAlert aggregate.
     * @param dtcAlert the aggregate to save
     * @return the saved aggregate
     */
    DtcAlert save(DtcAlert dtcAlert);

    /**
     * Saves multiple DtcAlert aggregates.
     * @param dtcAlerts the list of aggregates to save
     * @return the list of saved aggregates
     */
    List<DtcAlert> saveAll(List<DtcAlert> dtcAlerts);

    List<DtcAlert> findAllByRegistrationId(Obd2DeviceRegistrationId registrationId);

    List<DtcAlert> findAllByRegistrationId(Obd2DeviceRegistrationId registrationId, int page, int size);

    List<DtcAlert> findAllByRegistrationIdAndCreatedAtGreaterThanEqual(
            Obd2DeviceRegistrationId registrationId,
            Instant startTimestamp
    );

    List<DtcAlert> findAllByRegistrationIdAndCreatedAtGreaterThanEqual(
            Obd2DeviceRegistrationId registrationId,
            Instant startTimestamp,
            int page,
            int size
    );
}