package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.values.PropertyKeys;
import java.util.List;
import java.util.stream.Stream;

public record StoppedField(String name, Field from, Field to, double duration) implements Field {

    public StoppedField(double duration) {
        this(
                "stopped_" + duration,
                new ShiftedField(PropertyKeys.dist, duration),
                new ValueField(PropertyKeys.dist),
                duration);
    }

    public static Stream<Field> stopped(List<Double> doubles) {
        return doubles.stream().map(duration -> (Field) new StoppedField(duration));
    }

    @Override
    public double getValue(GPXPath gpxPath, double t) {
        double dist = to.getValue(gpxPath, t) - from.getValue(gpxPath, t);
        // < 0.5 m.s-1
        if (dist < duration / 2) {
            return 1.0;
        } else {
            return 0.0;
        }
    }
}
