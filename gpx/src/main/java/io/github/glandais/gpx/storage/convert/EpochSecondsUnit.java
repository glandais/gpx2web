package io.github.glandais.gpx.storage.convert;

import io.github.glandais.gpx.storage.Unit;

import java.time.Instant;

public class EpochSecondsUnit extends ConvertableUnit<Instant, Double> {

    public EpochSecondsUnit() {
        super(Unit.INSTANT);
    }

    @Override
    public Double convertFromStorage(Instant from) {
        return from.toEpochMilli() / 1000.0;
    }

    @Override
    public Instant convertToStorage(Double value) {
        return Instant.ofEpochMilli((long) (1000.0 * value));
    }

}
