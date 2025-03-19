package io.github.glandais.gpx.data;

import java.util.List;
import java.util.stream.DoubleStream;

public record GPX(String name, List<GPXPath> paths, List<GPXWaypoint> waypoints) {

    public double getDist() {
        return paths.stream().mapToDouble(GPXPath::getDist).sum();
    }

    public double getTotalElevation() {
        return paths.stream().mapToDouble(GPXPath::getTotalElevation).sum();
    }

    public double getTotalElevationNegative() {
        return paths.stream().mapToDouble(GPXPath::getTotalElevationNegative).sum();
    }

    private DoubleStream mergeLon(DoubleStream pathLon) {
        return DoubleStream.concat(
                pathLon, waypoints.stream().mapToDouble(w -> w.point().getLon()));
    }

    public double getMinlonDeg() {
        return mergeLon(paths.stream().mapToDouble(GPXPath::getMinlonDeg)).min().orElse(0.0);
    }

    public double getMaxlonDeg() {
        return mergeLon(paths.stream().mapToDouble(GPXPath::getMaxlonDeg)).max().orElse(0.0);
    }

    private DoubleStream mergeLat(DoubleStream pathLon) {
        return DoubleStream.concat(
                pathLon, waypoints.stream().mapToDouble(w -> w.point().getLat()));
    }

    public double getMinlatDeg() {
        return mergeLat(paths.stream().mapToDouble(GPXPath::getMinlatDeg)).min().orElse(0.0);
    }

    public double getMaxlatDeg() {
        return mergeLat(paths.stream().mapToDouble(GPXPath::getMaxlatDeg)).max().orElse(0.0);
    }
}
