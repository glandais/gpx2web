package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKeys;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

import java.util.List;

public class DataPoint implements Tuple {

    public static final List<Field> FIELDS = List.of(
            new Field("hr", PropertyKeys.heartRate, 0.0),

            new Field("hr5", PropertyKeys.heartRate, 5.0),
            new Field("hr10", PropertyKeys.heartRate, 10.0),
            new Field("hr30", PropertyKeys.heartRate, 30.0),
            new Field("hr60", PropertyKeys.heartRate, 60.0),

            new Field("p5", PropertyKeys.power, 0.0, 5.0),
            new Field("p10", PropertyKeys.power, 0.0, 10.0),
            new Field("p20", PropertyKeys.power, 0.0, 20.0),
            new Field("p30", PropertyKeys.power, 0.0, 30.0),
            new Field("p60", PropertyKeys.power, 0.0, 60.0)

    );

    private static final StructType SCHEMA = new StructType(FIELDS.stream().map(Field::getStructField).toList());

    private final double[] data;

    public DataPoint(GPXPath gpxPath, double t) {
        this.data = new double[FIELDS.size()];
        int d = 0;
        for (Field field : FIELDS) {
            this.data[d++] = field.getValue(gpxPath, t);
        }
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
