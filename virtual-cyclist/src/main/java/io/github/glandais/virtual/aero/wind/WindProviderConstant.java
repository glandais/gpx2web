package io.github.glandais.virtual.aero.wind;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class WindProviderConstant implements WindProvider {

    private final Wind wind;

    @Override
    public Wind getWind(Course course, Point location, CyclistStatus status) {
        return wind;
    }

}
