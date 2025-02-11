package io.github.glandais.gpx.data.values.unit;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

public class InstantUnit extends StorageUnit<Instant> {

    @Override
    public Instant interpolate(Instant v, Instant vp1, double coef) {

        return Instant.ofEpochMilli((long) (v.toEpochMilli() + coef * (vp1.toEpochMilli() - v.toEpochMilli())));
    }

    @Override
    public String formatData(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    @Override
    public String formatHuman(Instant instant) {
        return DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    @Override
    public String getFormat() {
        return null;
    }

    @Override
    public String getFormulaPartHumanToSI() {
        return null;
    }

    @Override
    public String getFormulaPartSIToHuman() {
        return null;
    }
}
