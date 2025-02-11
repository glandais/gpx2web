package io.github.glandais.gpx.climb;

import java.util.List;

public record Climb(
        double startDist,
        double startEle,
        double endDist,
        double endEle,
        double dist,
        double elevation,
        double positiveElevation,
        double negativeElevation,
        double grade,
        double climbingGrade,
        List<ClimbPart> parts
) {
}
