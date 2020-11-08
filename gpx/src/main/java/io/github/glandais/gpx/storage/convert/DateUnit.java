package io.github.glandais.gpx.storage.convert;

import io.github.glandais.gpx.storage.Unit;

import java.time.Instant;
import java.util.Date;

public class DateUnit extends ConvertableUnit<Instant, Date> {

    public DateUnit() {
        super(Unit.INSTANT);
    }

    @Override
    public Date convertFromStorage(Instant from) {
        return Date.from(from);
    }

    @Override
    public Instant convertToStorage(Date value) {
        return value.toInstant();
    }

}
