package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKey;
import io.github.glandais.gpx.data.values.unit.DoubleUnit;
import smile.data.type.DataTypes;
import smile.data.type.StructField;

public record Field(String name, PropertyKey<Double, DoubleUnit> propertyKey, boolean hr, double start, double end) {

    public Field(String name, PropertyKey<Double, DoubleUnit> propertyKey, double start, double end) {
        this(name, propertyKey, propertyKey.getPropertyKeyName().equals("heartRate"), start, end);
    }

    public Field(String name, PropertyKey<Double, DoubleUnit> propertyKey, double start) {
        this(name, propertyKey, start, start);
    }

    StructField getStructField() {
        return new StructField(name, DataTypes.DoubleType);
    }

    double getValue(GPXPath gpxPath, double t) {
        double average = gpxPath.getAverage(t - start, t - end, propertyKey);
        return hr ? clamp(average) : average;
    }

    private double clamp(double value) {
        return Math.max(60, Math.min(220, value));
    }

    String getConstructor() {
        String c = "new Field(\"" + name + "\", PropertyKeys." + propertyKey.getPropertyKeyName() + ", " + start;
        if (start == end) {
            c = c + "),";
        } else {
            c = c + ", " + end + "),";
        }
        return c;
    }
}
