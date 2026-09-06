package com.tuxlogic.shiftiq.platform.iot.application.internal.eventhandlers;

import com.tuxlogic.shiftiq.platform.iot.domain.model.events.DtcAlertTriggeredEvent;
import com.tuxlogic.shiftiq.platform.iot.domain.model.valueobjects.DtcAlertSeverity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Reacts to newly generated DTC alerts once the ingestion transaction has
 * committed. For CRITICAL/HIGH severity alerts, logs a structured warning
 * so it can be picked up by monitoring/alerting tooling.
 */
@Component
public class DtcAlertEventListener {

    private static final Logger log = LoggerFactory.getLogger(DtcAlertEventListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void on(DtcAlertTriggeredEvent event) {
        if (DtcAlertSeverity.CRITICAL.equals(event.severity()) || DtcAlertSeverity.HIGH.equals(event.severity())) {
            log.warn(
                    "High-severity DTC alert triggered: alertId={}, branchId={}, telemetrySnapshotId={}, dtcCode={}, severity={}",
                    event.dtcAlertId(),
                    event.branchId().value(),
                    event.telemetrySnapshotId().value(),
                    event.dtcCode(),
                    event.severity().value()
            );
        } else {
            log.info(
                    "DTC alert triggered: alertId={}, dtcCode={}, severity={}",
                    event.dtcAlertId(),
                    event.dtcCode(),
                    event.severity().value()
            );
        }
    }
}