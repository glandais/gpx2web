package io.github.glandais.gpx.storage.convert;

import io.github.glandais.gpx.storage.Unit;

import java.time.Instant;

public class EpochMillisUnit extends ConvertableUnit<Instant, Long> {

    public EpochMillisUnit() {
        super(Unit.INSTANT);
    }

    @Override
    public Long convertFromStorage(Instant from) {
        return from.toEpochMilli();
    }

    @Override
    public Instant convertToStorage(Long value) {
        return Instant.ofEpochMilli(value);
    }

}
