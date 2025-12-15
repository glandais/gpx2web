package io.github.glandais.gpx.virtual.heartrate;

import io.github.glandais.gpx.data.GPXPath;

public interface Field {
    double getValue(GPXPath gpxPath, double t);

    String name();
}
