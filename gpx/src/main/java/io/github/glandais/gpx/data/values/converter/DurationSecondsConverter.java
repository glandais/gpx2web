package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.DurationUnit;
import java.time.Duration;

public class DurationSecondsConverter implements Converter<Duration, DurationUnit, Double> {

    @Override
    public Double convertFromStorage(Duration storageValue) {
        long seconds = storageValue.getSeconds();
        int nanoAdjustment = storageValue.getNano();
        return seconds + (nanoAdjustment / 1_000_000_000.0);
    }

    @Override
    public Duration convertToStorage(Double value) {
        long seconds = value.longValue();
        double fractionalSeconds = value - seconds;
        long nanos = (long) (fractionalSeconds * 1_000_000_000);
        return Duration.ofSeconds(seconds, nanos);
    }
}
