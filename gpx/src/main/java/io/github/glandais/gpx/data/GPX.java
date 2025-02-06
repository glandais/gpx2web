package io.github.glandais.gpx.data;

import java.util.List;

public record GPX(String name, List<GPXPath> paths, List<GPXWaypoint> waypoints) {

}
