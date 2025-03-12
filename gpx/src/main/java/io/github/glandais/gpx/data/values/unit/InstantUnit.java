package io.github.glandais.gpx.data.values.unit;

import java.time.Duration;
import java.time.Instant;

public class InstantUnit implements Unit<Instant> {
    public static final InstantUnit INSTANCE = new InstantUnit();

    @Override
    public Instant interpolate(Instant v, Instant vp1, double coef) {
        Duration d = Duration.between(v, vp1);
        Duration interpolate = DurationUnit.INSTANCE.interpolate(Duration.ZERO, d, coef);
        return v.plus(interpolate);
    }

    @Override
    public String formatHuman(Instant value) {
        if (value == null) {
            return "";
        }
        return value.toString();
    }
}
