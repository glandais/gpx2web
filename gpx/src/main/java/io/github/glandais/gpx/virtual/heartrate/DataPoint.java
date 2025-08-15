package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKeys;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import smile.data.Tuple;
import smile.data.type.DataTypes;
import smile.data.type.StructField;
import smile.data.type.StructType;

@Slf4j
public class DataPoint implements Tuple {

    private static final Stream<Field> SINGULAR_FIELDS = Stream.of(
            new ValueField(PropertyKeys.heartRate),
            new ValueField(PropertyKeys.cadence),
            new ValueField(PropertyKeys.power),
            new ValueField(PropertyKeys.ele)
            );

    public static final List<Field> FIELDS = Stream.of(
                    SINGULAR_FIELDS,
                    MovingAverageField.movingAveraged(PropertyKeys.cadence, List.of(5.0, 10.0, 30.0, 60.0)),
                    MovingAverageField.movingAveraged(PropertyKeys.power, List.of(5.0, 10.0, 30.0, 60.0)),
                    ShiftedField.shifted(PropertyKeys.cadence, List.of(1.0, 5.0, 10.0, 15.0, 30.0)),
                    ShiftedField.shifted(PropertyKeys.power, List.of(1.0, 5.0, 10.0, 15.0, 30.0))
            )
            .flatMap(Function.identity())
            .toList();

    private static final StructType SCHEMA =
            new StructType(FIELDS.stream().map(DataPoint::getStructField).toList());

    static StructField getStructField(Field field) {
        return new StructField(field.name(), DataTypes.DoubleType);
    }

    private final double[] data;

    public DataPoint(GPXPath gpxPath, double t) {
        this.data = new double[FIELDS.size()];
        int d = 0;
        for (Field field : FIELDS) {
            this.data[d++] = field.getValue(gpxPath, t);
        }
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
