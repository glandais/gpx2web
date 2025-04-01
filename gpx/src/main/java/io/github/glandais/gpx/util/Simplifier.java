package io.github.glandais.gpx.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class Simplifier<P> {

    private final Function<P, Vector> toR3;

    public Simplifier(Function<P, Vector> toR3) {
        this.toR3 = toR3;
    }

    /**
     * Simplification using Ramer-Douglas-Peucker algorithm.
     *
     * @param points a list of points to be simplified
     * @param tolerance tolerance (meters)
     * @return a list of simplified points
     */
    public List<P> douglasPeucker(List<P> points, double tolerance) {

        if (points.size() <= 2) {
            return points;
        }

        int last = points.size() - 1;
        List<P> simplified = new ArrayList<>();
        simplified.add(points.get(0));
        simplified.addAll(simplifyDpStep(points, 0, last, tolerance));
        simplified.add(points.get(last));
        return simplified;
    }

    private List<P> simplifyDpStep(List<P> points, int first, int last, double tolerance) {

        double maxDist = tolerance;
        int index = 0;

        List<P> stepList = new ArrayList<>();

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

    private double getSegDist(P point, P p1, P p2) {
        Vector v = toR3.apply(point);
        Vector a = toR3.apply(p1);
        Vector b = toR3.apply(p2);

        return Vector.distanceToSegment(v, a, b);
    }
}
