package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;

public record ValueField(String name, PropertyKey<Double, DoubleUnit> propertyKey, boolean hr) implements Field {

    public ValueField(PropertyKey<Double, DoubleUnit> propertyKey) {
        this(
                propertyKey.getPropertyKeyName(),
                propertyKey,
                propertyKey.getPropertyKeyName().equals("heartRate"));
    }

    @Override
    public double getValue(GPXPath gpxPath, double t) {
        double average = gpxPath.getAverage(t, t, propertyKey);
        return hr ? clamp(average) : average;
    }

    private double clamp(double value) {
        return Math.max(60, Math.min(220, value));
    }
}
