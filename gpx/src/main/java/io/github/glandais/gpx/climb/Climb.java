package io.github.glandais.gpx.climb;

public record Climb(
        int i,
        int j,
        double score,
        double startDist,
        double startEle,
        double endDist,
        double endEle,
        double positiveElevation,
        double negativeElevation
) {
}
