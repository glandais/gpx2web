package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import java.util.List;
import java.util.stream.Stream;

public record ShiftedField(String name, PropertyKey<Double, DoubleUnit> propertyKey, boolean hr, double shift)
        implements Field {

    public ShiftedField(PropertyKey<Double, DoubleUnit> propertyKey, double shift) {
        this(
                propertyKey.getPropertyKeyName() + "_shift_" + shift,
                propertyKey,
                propertyKey.getPropertyKeyName().equals("heartRate"),
                shift);
    }

    public static Stream<Field> shifted(PropertyKey<Double, DoubleUnit> propertyKey, List<Double> doubles) {
        return doubles.stream().map(shift -> (Field) new ShiftedField(propertyKey, shift));
    }

    @Override
    public double getValue(GPXPath gpxPath, double t) {
        double average = gpxPath.getAverage(t - shift, t - shift, propertyKey);
        return hr ? clamp(average) : average;
    }

    private double clamp(double value) {
        return Math.max(60, Math.min(220, value));
    }
}
