package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.InstantUnit;

import java.time.Instant;

public class EpochSecondsConverter implements Converter<Instant, InstantUnit, Double> {

    @Override
    public Double convertFromStorage(Instant storageValue) {
        long seconds = storageValue.getEpochSecond();
        int nanoAdjustment = storageValue.getNano();
        return seconds + (nanoAdjustment / 1_000_000_000.0);
    }

    @Override
    public Instant convertToStorage(Double value) {
        long seconds = value.longValue();
        double fractionalSeconds = value - seconds;
        long nanos = (long) (fractionalSeconds * 1_000_000_000);
        return Instant.ofEpochSecond(seconds, nanos);
    }
}
