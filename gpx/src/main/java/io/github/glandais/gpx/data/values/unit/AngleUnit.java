package io.github.glandais.gpx.data.values.unit;

import io.github.glandais.gpx.data.values.converter.Converters;

/** Radians */
public class AngleUnit extends DoubleUnit {
    public static final AngleUnit INSTANCE = new AngleUnit();

    @Override
    public String formatHuman(Double value) {
        if (value == null) {
            return "";
        }
        return super.formatHuman(Converters.DEGREES_CONVERTER.convertFromStorage(value));
    }
}
