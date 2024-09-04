package io.github.glandais.gpx.climb;

record DetectedClimb(
        int i,
        int j,
        double score,
        double startDist,
        double startEle,
        double endDist,
        double endEle,
        double dist,
        double elevation,
        double positiveElevation,
        double negativeElevation,
        double grade,
        double climbingGrade
) {
}
