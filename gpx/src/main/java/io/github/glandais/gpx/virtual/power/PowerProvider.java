package io.github.glandais.gpx.virtual.power;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;

public interface PowerProvider {

    double getPowerW(Course course, Point location);

    PowerProviderId getId();
}
