package io.github.glandais.gpx.climb;

import java.util.ArrayList;
import java.util.List;

class RamerDouglasPeucker {

    private RamerDouglasPeucker() {
    }

    private static double sqr(double x) {
        return Math.pow(x, 2);
    }

    private static double distanceBetweenPoints(double vx, double vy, double wx, double wy) {
        return sqr(vx - wx) + sqr(vy - wy);
    }

    private static double distanceToSegmentSquared(double px, double py, double vx, double vy, double wx, double wy) {
        final double l2 = distanceBetweenPoints(vx, vy, wx, wy);
        if (l2 == 0)
            return distanceBetweenPoints(px, py, vx, vy);
        final double t = ((px - vx) * (wx - vx) + (py - vy) * (wy - vy)) / l2;
        if (t < 0)
            return distanceBetweenPoints(px, py, vx, vy);
        if (t > 1)
            return distanceBetweenPoints(px, py, wx, wy);
        return distanceBetweenPoints(px, py, (vx + t * (wx - vx)), (vy + t * (wy - vy)));
    }

    private static double perpendicularDistance(double px, double py, double vx, double vy, double wx, double wy) {
        return Math.sqrt(distanceToSegmentSquared(px, py, vx, vy, wx, wy));
    }

    private static void douglasPeucker(List<ClimbPoint> list, int s, int e, double epsilon, List<ClimbPoint> resultList) {
        // Find the point with the maximum distance
        double dmax = 0;
        int index = 0;

        final int start = s;
        final int end = e - 1;
        for (int i = start + 1; i < end; i++) {
            // Point
            final double px = list.get(i).dist();
            final double py = list.get(i).ele();
            // Start
            final double vx = list.get(start).dist();
            final double vy = list.get(start).ele();
            // End
            final double wx = list.get(end).dist();
            final double wy = list.get(end).ele();
            final double d = perpendicularDistance(px, py, vx, vy, wx, wy);
            if (d > dmax) {
                index = i;
                dmax = d;
            }
        }
        // If max distance is greater than epsilon, recursively simplify
        if (dmax > epsilon) {
            // Recursive call
            douglasPeucker(list, s, index, epsilon, resultList);
            douglasPeucker(list, index, e, epsilon, resultList);
        } else {
            if ((end - start) > 0) {
                resultList.add(list.get(start));
                resultList.add(list.get(end));
            } else {
                resultList.add(list.get(start));
            }
        }
    }

    public static List<ClimbPoint> douglasPeucker(List<ClimbPoint> list, double epsilon) {
        final List<ClimbPoint> resultList = new ArrayList<>();
        douglasPeucker(list, 0, list.size(), epsilon, resultList);
        return resultList;
    }
}