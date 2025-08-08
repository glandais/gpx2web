package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.InstantUnit;
import java.time.Instant;
import java.util.Date;

public class DateConverter implements Converter<Instant, InstantUnit, Date> {

    @Override
    public Date convertFromStorage(Instant storageValue) {
        return storageValue == null ? null : Date.from(storageValue);
    }

    @Override
    public Instant convertToStorage(Date value) {
        return value == null ? null : value.toInstant();
    }
}
