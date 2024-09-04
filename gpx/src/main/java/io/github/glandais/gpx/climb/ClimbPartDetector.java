package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.GPXPath;

import java.util.ArrayList;
import java.util.List;

class ClimbPartDetector {

    private enum MergeWith {
        PREVIOUS,
        NEXT
    }

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
        points = RamerDouglasPeucker.douglasPeucker(points, 3.0);
        points = getClimbPointsMinimum100m(points);
        points = getClimbPointsMergeSameGrades(points);

        return points;
    }

    private List<ClimbPoint> getClimbPointsMinimum100m(List<ClimbPoint> pointsParam) {
        List<ClimbPoint> points = pointsParam;
        boolean removed = true;
        while (removed) {
            removed = false;
            if (points.size() > 2) {
                // remove shortest < 100m
                int iShortest = -1;
                double shortestDist = 100;
                for (int i = 0; i < points.size() - 1; i++) {
                    double dist = points.get(i + 1).dist() - points.get(i).dist();
                    if (dist < shortestDist) {
                        iShortest = i;
                        shortestDist = dist;
                    }
                }

                if (iShortest == -1) {
                    removed = false;
                } else if (iShortest == 0) {
                    points = merge(points, iShortest, MergeWith.NEXT);
                } else if (iShortest == points.size() - 2) {
                    points = merge(points, iShortest, MergeWith.PREVIOUS);
                    removed = true;
                } else {
                    double ddistm1 = Math.abs(points.get(iShortest - 1).dist() - points.get(iShortest).dist());
                    double ddistp1 = Math.abs(points.get(iShortest + 1).dist() - points.get(iShortest + 2).dist());
                    if (ddistm1 > ddistp1) {
                        points = merge(points, iShortest, MergeWith.PREVIOUS);
                        removed = true;
                    } else {
                        points = merge(points, iShortest, MergeWith.NEXT);
                        removed = true;
                    }
                }
            }
        }
        return points;
    }

    private List<ClimbPoint> getClimbPointsMergeSameGrades(List<ClimbPoint> pointsParam) {

        List<ClimbPoint> points = pointsParam;
        boolean removed = true;
        while (removed) {
            removed = false;

            int iMinDiff = -1;
            double minDiff = 100;

            for (int i = 0; i < points.size() - 2; i++) {
                double grade = 100 * (points.get(i + 1).ele() - points.get(i).ele()) / (points.get(i + 1).dist() - points.get(i).dist());
                double gradep1 = 100 * (points.get(i + 2).ele() - points.get(i + 1).ele()) / (points.get(i + 2).dist() - points.get(i + 1).dist());

                double diff = Math.abs(gradep1 - grade);
                if (diff <= 1.0 && diff < minDiff) {
                    minDiff = diff;
                    iMinDiff = i;
                }
            }

            if (iMinDiff >= 0) {
                removed = true;
                points = merge(points, iMinDiff, MergeWith.NEXT);
            }
        }

        return points;
    }

    private List<ClimbPoint> merge(List<ClimbPoint> points, int i, MergeWith with) {
        List<ClimbPoint> result = new ArrayList<>(points.size());
        int iExcluded;
        if (with == MergeWith.PREVIOUS) {
            iExcluded = i;
        } else {
            iExcluded = i + 1;
        }
        for (int j = 0; j < points.size(); j++) {
            if (j != iExcluded) {
                result.add(points.get(j));
            }
        }
        return result;
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
