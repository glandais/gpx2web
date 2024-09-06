package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.filter.R3;

import java.util.ArrayList;
import java.util.List;

class RamerDouglasPeucker {

    private RamerDouglasPeucker() {
    }

    /**
     * Simplification using Ramer-Douglas-Peucker algorithm.
     *
     * @param points    a list of points to be simplified
     * @param tolerance tolerance (meters)
     * @return a list of simplified points
     */
    static List<ClimbPoint> douglasPeucker(List<ClimbPoint> points, double tolerance) {

        if (points.size() <= 2) {
            return points;
        }

        int last = points.size() - 1;
        List<ClimbPoint> simplified = new ArrayList<>();
        simplified.add(points.get(0));
        simplified.addAll(simplifyDpStep(points, 0, last, tolerance));
        simplified.add(points.get(last));
        return simplified;
    }

    private static List<ClimbPoint> simplifyDpStep(List<ClimbPoint> points, int first, int last, double tolerance) {

        double maxDist = tolerance;
        int index = 0;

        List<ClimbPoint> stepList = new ArrayList<>();

        for (int i = first + 1; i < last; i++) {
            double dist = getSegDist(points.get(i), points.get(first), points.get(last));
            if (dist > maxDist) {
                index = i;
                maxDist = dist;
            }
        }

        if (maxDist > tolerance) {
            if (index - first > 1) {
                stepList.addAll(simplifyDpStep(points, first, index, tolerance));
            }

            stepList.add(points.get(index));

            if (last - index > 1) {
                stepList.addAll(simplifyDpStep(points, index, last, tolerance));
            }
        }

        return stepList;
    }

    private static double getSegDist(ClimbPoint point, ClimbPoint p1, ClimbPoint p2) {
        R3 v = getR3(point);
        R3 a = getR3(p1);
        R3 b = getR3(p2);

        return R3.distanceToSegment(v, a, b);
    }

    private static R3 getR3(ClimbPoint point) {
        return new R3(point.dist(), point.ele(), 0.0);
    }
}