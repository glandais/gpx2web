package io.github.glandais.gpx.climb;

import java.io.Serializable;

public record ClimbPart(
        double startDist, double startEle, double endDist, double endEle, double dist, double ele, double grade)
        implements Serializable {
    public ClimbPart shiftDist(double dx) {
        return new ClimbPart(startDist + dx, startEle, endDist + dx, endEle, dist, ele, grade);
    }
}
