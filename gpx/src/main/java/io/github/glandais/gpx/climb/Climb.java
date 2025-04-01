package io.github.glandais.gpx.climb;

import java.util.stream.Collectors;

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
        ClimbParts parts) {
    public Climb shiftDist(double dx) {
        return new Climb(
                startDist + dx,
                startEle,
                endDist + dx,
                endEle,
                dist,
                elevation,
                positiveElevation,
                negativeElevation,
                grade,
                climbingGrade,
                new ClimbParts(parts.stream().map(cp -> cp.shiftDist(dx)).collect(Collectors.toList())));
    }
}
