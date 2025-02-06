package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.data.GPXPath;

import java.util.ArrayList;
import java.util.List;

class ClimbPartDetector {

    public List<ClimbPart> getParts(GPXPath gpxPath, DetectedClimb detectedClimb) {
        double initialDist = gpxPath.getDists()[detectedClimb.i()];
        List<ClimbPoint> points = getClimbPoints(gpxPath, detectedClimb, initialDist);
        return getClimbParts(points);
    }

    private List<ClimbPoint> getClimbPoints(GPXPath gpxPath, DetectedClimb detectedClimb, double initialDist) {
        List<ClimbPoint> points = new ArrayList<>();
        for (int i = detectedClimb.i(); i <= detectedClimb.j(); i++) {
            double dist = gpxPath.getDists()[i] - initialDist;
            double ele = gpxPath.getEles()[i];
            points.add(new ClimbPoint(dist, ele));
        }
        double tolerance = detectedClimb.elevation() / 50;
        tolerance = Math.max(
                10,
                Math.min(
                        50, tolerance
                )
        );
        points = RamerDouglasPeucker.douglasPeucker(points, tolerance);

        return points;
    }

    private List<ClimbPart> getClimbParts(List<ClimbPoint> points) {
        List<ClimbPart> parts = new ArrayList<>();
        for (int i = 0; i < points.size() - 1; i++) {
            double startDist = points.get(i).dist();
            double startEle = points.get(i).ele();

            double endDist = points.get(i + 1).dist();
            double endEle = points.get(i + 1).ele();

            double elevation = endEle - startEle;

            parts.add(new ClimbPart(
                    startDist,
                    startEle,
                    endDist,
                    endEle,
                    endDist - startDist,
                    elevation,
                    100 * elevation / (endDist - startDist)
            ));
        }
        return parts;
    }
}
