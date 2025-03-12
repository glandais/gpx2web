package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.InstantUnit;

import java.time.Instant;

public class EpochMillisConverter implements Converter<Instant, InstantUnit, Long> {

    @Override
    public Long convertFromStorage(Instant storageValue) {
        return storageValue.toEpochMilli();
    }

    @Override
    public Instant convertToStorage(Long value) {
        return Instant.ofEpochMilli(value);
    }
}
