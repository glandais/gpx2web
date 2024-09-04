package io.github.glandais.gpx.climb;

public record ClimbPart(
        double startDist,
        double startEle,
        double endDist,
        double endEle,
        double dist,
        double ele,
        double grade
) {
}
