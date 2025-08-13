package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import java.util.List;
import java.util.stream.Stream;

public record MovingAverageField(String name, PropertyKey<Double, DoubleUnit> propertyKey, boolean hr, double length)
        implements Field {

    public MovingAverageField(PropertyKey<Double, DoubleUnit> propertyKey, double length) {
        this(
                propertyKey.getPropertyKeyName() + "_movingAverage_" + length,
                propertyKey,
                propertyKey.getPropertyKeyName().equals("heartRate"),
                length);
    }

    public static Stream<Field> movingAveraged(PropertyKey<Double, DoubleUnit> propertyKey, List<Double> doubles) {
        return doubles.stream().map(length -> (Field) new MovingAverageField(propertyKey, length));
    }

    @Override
    public double getValue(GPXPath gpxPath, double t) {
        double average = gpxPath.getAverage(t, t - length, propertyKey);
        return hr ? clamp(average) : average;
    }

    private double clamp(double value) {
        return Math.max(60, Math.min(220, value));
    }
}
