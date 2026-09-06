package com.tuxlogic.shiftiq.platform.iot.application.internal.queryservices;

import com.tuxlogic.shiftiq.platform.iot.application.queryservices.DtcAlertQueryService;
import com.tuxlogic.shiftiq.platform.iot.domain.model.aggregates.DtcAlert;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetDtcAlertsByRegistrationIdQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.model.queries.GetVehicleDtcAlertHistoryQuery;
import com.tuxlogic.shiftiq.platform.iot.domain.repositories.DtcAlertRepository;
import com.tuxlogic.shiftiq.platform.iot.domain.services.ActiveRegistrationContextService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service implementation for handling DTC Alert queries inside the iot context.
 */
@Service
public class DtcAlertQueryServiceImpl implements DtcAlertQueryService {

    private final DtcAlertRepository dtcAlertRepository;
    private final ActiveRegistrationContextService activeRegistrationContextService;

    public DtcAlertQueryServiceImpl(
            DtcAlertRepository dtcAlertRepository,
            ActiveRegistrationContextService activeRegistrationContextService
    ) {
        this.dtcAlertRepository = dtcAlertRepository;
        this.activeRegistrationContextService = activeRegistrationContextService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtcAlert> handle(GetDtcAlertsByRegistrationIdQuery query) {
        return dtcAlertRepository.findAllByRegistrationId(query.obd2DeviceRegistrationId(), query.page(), query.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DtcAlert> handle(GetVehicleDtcAlertHistoryQuery query) {
        return activeRegistrationContextService.resolveActiveContextForVehicle(query.vehicleId())
                .map(ctx -> dtcAlertRepository.findAllByRegistrationIdAndCreatedAtGreaterThanEqual(
                        ctx.obd2DeviceRegistrationId(),
                        ctx.startTimestamp(),
                        query.page(),
                        query.size()
                ))
                .orElseGet(List::of);
    }
}
