package rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.events;

import java.time.Instant;
import rs.ac.uns.acs.nais.TimeseriesDatabaseService.model.enums.SessionEventType;

public record AppSessionEvent(
        String userId,
        String device,
        String appVersion,
        SessionEventType event,
        Double durationSec,   // popunjava se samo za END
        Instant ts,
        String idempotencyKey
) { }
