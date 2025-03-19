package io.github.glandais.gpx.data.values.converter;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Converters {
    public static final DateConverter DATE_CONVERTER = new DateConverter();
    public static final DegreesConverter DEGREES_CONVERTER = new DegreesConverter();
    public static final DurationSecondsConverter DURATION_SECONDS_CONVERTER = new DurationSecondsConverter();
    public static final SemiCirclesConverter SEMI_CIRCLES_CONVERTER = new SemiCirclesConverter();
}
