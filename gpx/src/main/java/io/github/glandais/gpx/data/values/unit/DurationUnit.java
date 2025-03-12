package io.github.glandais.gpx.data.values.unit;

import io.github.glandais.gpx.data.values.converter.Converters;

import java.time.Duration;

public class DurationUnit implements Unit<Duration> {
    public static final DurationUnit INSTANCE = new DurationUnit();

    @Override
    public Duration interpolate(Duration v, Duration vp1, double coef) {
        long nanosD1 = v.toNanos();
        long nanosD2 = vp1.toNanos();

        long diffNanos = nanosD2 - nanosD1;

        long interpolatedNanos = nanosD1 + (long) (coef * diffNanos);

        return Duration.ofNanos(interpolatedNanos);
    }

    @Override
    public String formatHuman(Duration value) {
        if (value == null) {
            return "";
        }
        return Converters.DURATION_SECONDS_CONVERTER.convertFromStorage(value).toString();
    }
}
