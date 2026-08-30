package com.tuxlogic.shiftiq.platform.iot.application.queryservices;

import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetDtcAlertsByRegistrationIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;

import java.util.List;

/**
 * Service interface for handling DTC Alert queries inside the iot context.
 */
public interface DtcAlertQueryService {

    /**
     * Handles retrieving all DTC alerts for a specific OBD2 device registration.
     * @param query the query containing the registration ID
     * @return the list of DTC alerts ordered by creation date descending
     */
    List<DtcAlert> handle(GetDtcAlertsByRegistrationIdQuery query);

    /**
     * Handles retrieving the historical DTC alerts for a vehicle starting from its active driver registration start date.
     * @param query the query containing the vehicle identifier
     * @return the list of DTC alerts ordered by creation date descending
     */
    List<DtcAlert> handle(GetVehicleDtcAlertHistoryQuery query);
}
