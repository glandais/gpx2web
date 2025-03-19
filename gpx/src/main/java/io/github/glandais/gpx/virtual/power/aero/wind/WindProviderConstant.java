package io.github.glandais.gpx.virtual.power.aero.wind;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WindProviderConstant implements WindProvider {

    private final Wind wind;

    @Override
    public Wind getWind(Course course, Point location) {
        return wind;
    }
}
