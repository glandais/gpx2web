package io.github.glandais.gpx;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GPXFilter {

    public void filterPointsDouglasPeucker(GPXPath path) {

        simplify(path, false, true);
        path.computeArrays();
    }

    public void filterPointsDistance(GPXPath path) {

        simplify(path, true, false);
        path.computeArrays();
    }

    private void simplify(GPXPath path, boolean radial, boolean douglasPeucker) {

        List<Point> points = path.getPoints();

        log.info("Filtering {} ({})", path.getName(), points.size());
        List<Point> newPoints = simplify(points, 3, radial, douglasPeucker);
        path.setPoints(newPoints);
        log.info("Filtered {} ({} -> {})", path.getName(), points.size(), newPoints.size());
    }

    /**
     * Reduces the number of points in a polyline while retaining its shape, giving a performance boost when processing it and also reducing
     * visual noise.
     *
     * @param points an array of points
     * @param tolerance meters
     * @return an array of simplified points
     * @see <a href="http://mourner.github.io/simplify-js/">JavaScript
     * implementation</a>
     * @since 1.2.0
     */
    protected List<Point> simplify(List<Point> points, double tolerance, boolean radial, boolean douglasPeucker) {

        if (points.size() <= 2) {
            return points;
        }

        // 360° = 40000km
        final double toleranceDeg = tolerance / (40000.0 * 1000.0 / 360.0);
        final double sqTolerance = toleranceDeg * toleranceDeg;

        points = radial ? simplifyRadialDist(points, tolerance) : points;
        points = douglasPeucker ? simplifyDouglasPeucker(points, sqTolerance) : points;

        return points;
    }

    /**
     * Basic distance-based simplification.
     *
     * @param points a list of points to be simplified
     * @param tolerance meters
     * @return a list of simplified points
     */
    public static List<Point> simplifyRadialDist(List<Point> points, double tolerance) {

        Point prevPoint = points.get(0);
        ArrayList<Point> newPoints = new ArrayList<>();
        newPoints.add(prevPoint);
        Point point = null;

        for (int i = 1, len = points.size(); i < len; i++) {
            point = points.get(i);

            if (point.distanceTo(prevPoint) * 1000.0 > tolerance) {
                newPoints.add(point);
                prevPoint = point;
            }
        }

        if (!prevPoint.equals(point)) {
            newPoints.add(point);
        }
        return newPoints;
    }

    /**
     * Square distance from a point to a segment.
     *
     * @param point {@link Point} whose distance from segment needs to be determined
     * @param p1,p2 points defining the segment
     * @return square of the distance between first input point and segment defined by other two input points
     */
    private static double getSqSegDist(Point point, Point p1, Point p2) {

        double horizontal = p1.getLon();
        double vertical = p1.getLat();
        double diffHorizontal = p2.getLon() - horizontal;
        double diffVertical = p2.getLat() - vertical;

        if (diffHorizontal != 0 || diffVertical != 0) {
            double total = ((point.getLon() - horizontal) * diffHorizontal + (point.getLat() - vertical) * diffVertical) /
                    (diffHorizontal * diffHorizontal + diffVertical * diffVertical);
            if (total > 1) {
                horizontal = p2.getLon();
                vertical = p2.getLat();

            } else if (total > 0) {
                horizontal += diffHorizontal * total;
                vertical += diffVertical * total;
            }
        }

        diffHorizontal = point.getLon() - horizontal;
        diffVertical = point.getLat() - vertical;

        return diffHorizontal * diffHorizontal + diffVertical * diffVertical;
    }

    private static List<Point> simplifyDpStep(List<Point> points, int first, int last, double sqTolerance) {

        double maxSqDist = sqTolerance;
        int index = 0;

        ArrayList<Point> stepList = new ArrayList<>();

        for (int i = first + 1; i < last; i++) {
            double sqDist = getSqSegDist(points.get(i), points.get(first), points.get(last));
            if (sqDist > maxSqDist) {
                index = i;
                maxSqDist = sqDist;
            }
        }

        if (maxSqDist > sqTolerance) {
            if (index - first > 1) {
                stepList.addAll(simplifyDpStep(points, first, index, sqTolerance));
            }

            stepList.add(points.get(index));

            if (last - index > 1) {
                stepList.addAll(simplifyDpStep(points, index, last, sqTolerance));
            }
        }

        return stepList;
    }

    /**
     * Simplification using Ramer-Douglas-Peucker algorithm.
     *
     * @param points a list of points to be simplified
     * @param sqTolerance square of amount of simplification
     * @return a list of simplified points
     */
    protected static List<Point> simplifyDouglasPeucker(List<Point> points, double sqTolerance) {

        int last = points.size() - 1;
        ArrayList<Point> simplified = new ArrayList<>();
        simplified.add(points.get(0));
        simplified.addAll(simplifyDpStep(points, 0, last, sqTolerance));
        simplified.add(points.get(last));
        return simplified;
    }
}
