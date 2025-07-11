package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKeys;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

public class DataPoint implements Tuple {

    private static final StructType SCHEMA = new StructType(
            new StructField("hr", DataTypes.DoubleType),
            new StructField("p5", DataTypes.DoubleType),
            new StructField("p10", DataTypes.DoubleType),
            new StructField("p30", DataTypes.DoubleType),
            new StructField("p60", DataTypes.DoubleType),
            new StructField("hr5", DataTypes.DoubleType),
            new StructField("hr10", DataTypes.DoubleType),
            new StructField("hr30", DataTypes.DoubleType),
            new StructField("hr60", DataTypes.DoubleType)
    );
    private final double[] data;

    public DataPoint(GPXPath gpxPath, double t) {
        this.data = new double[]{
                gpxPath.getAverage(t, t + 0.01, PropertyKeys.heartRate),
                gpxPath.getAverage(t - 5, t, PropertyKeys.power),
                gpxPath.getAverage(t - 10, t, PropertyKeys.power),
                gpxPath.getAverage(t - 30, t, PropertyKeys.power),
                gpxPath.getAverage(t - 60, t, PropertyKeys.power),
                hr(gpxPath.getAverage(t - 5.01, t - 5, PropertyKeys.heartRate)),
                hr(gpxPath.getAverage(t - 10.01, t - 10, PropertyKeys.heartRate)),
                hr(gpxPath.getAverage(t - 30.01, t - 30, PropertyKeys.heartRate)),
                hr(gpxPath.getAverage(t - 60.01, t - 60, PropertyKeys.heartRate))
        };
    }

    private double hr(double value) {
        return Math.max(60, Math.min(220, value));
    }

    @Override
    public StructType schema() {
        return SCHEMA;
    }

    @Override
    public Object get(int i) {
        return this.data[i];
    }
}
