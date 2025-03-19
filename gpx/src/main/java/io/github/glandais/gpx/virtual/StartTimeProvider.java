package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.data.Point;
import jakarta.inject.Singleton;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import net.iakovlev.timeshape.TimeZoneEngine;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class StartTimeProvider {

    private final TimeZoneEngine timeZoneEngine;

    public StartTimeProvider() {
        this.timeZoneEngine = TimeZoneEngine.initialize();
    }

    public Instant getStart(Point point, int ddays) {
        ZoneId zoneId =
                timeZoneEngine.query(point.getLatDeg(), point.getLonDeg()).orElse(ZoneOffset.UTC);
        ZonedDateTime now = ZonedDateTime.now(zoneId);
        ZonedDateTime tomorrowAt8AM =
                now.plusDays(ddays).withHour(8).withMinute(0).withSecond(0).withNano(0);
        return tomorrowAt8AM.toInstant();
    }
}
