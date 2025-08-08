package io.github.glandais.gpx.data.values.converter;

import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Converters {
    public static final DateConverter DATE_CONVERTER = new DateConverter();
    public static final DegreesConverter DEGREES_CONVERTER = new DegreesConverter();
    public static final DurationSecondsConverter DURATION_SECONDS_CONVERTER = new DurationSecondsConverter();
    public static final NoopConverter<Double, DoubleUnit> NOOP_CONVERTER = new NoopConverter<>();
    public static final SemiCirclesConverter SEMI_CIRCLES_CONVERTER = new SemiCirclesConverter();
}
