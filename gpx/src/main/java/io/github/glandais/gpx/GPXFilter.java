package io.github.glandais.gpx;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class GPXFilter {

    private static final double TOLERANCE = 3.0;

    private static final double a = 6378137.0;              //WGS-84 semi-major axis
    private static final double e2 = 6.6943799901377997e-3;  //WGS-84 first eccentricity squared

    public static void filterPointsDouglasPeucker(GPXPath path) {
        filterPointsDouglasPeucker(path, TOLERANCE);
    }

    public static void filterPointsDouglasPeucker(GPXPath path, double tolerance) {

        List<Point> points = path.getPoints();

        if (points.size() <= 2) {
            return;
        }

        log.info("Filtering {} ({})", path.getName(), points.size());

        List<Point> newPoints = simplifyDouglasPeucker(points, tolerance);
        path.setPoints(newPoints);
        log.info("Filtered {} ({} -> {})", path.getName(), points.size(), newPoints.size());
        path.computeArrays();
    }

    /**
     * Simplification using Ramer-Douglas-Peucker algorithm.
     *
     * @param points    a list of points to be simplified
     * @param tolerance tolerance (meters)
     * @return a list of simplified points
     */
    protected static List<Point> simplifyDouglasPeucker(List<Point> points, double tolerance) {

        int last = points.size() - 1;
        ArrayList<Point> simplified = new ArrayList<>();
        simplified.add(points.get(0));
        simplified.addAll(simplifyDpStep(points, 0, last, tolerance));
        simplified.add(points.get(last));
        return simplified;
    }

    private static List<Point> simplifyDpStep(List<Point> points, int first, int last, double tolerance) {

        double maxDist = tolerance;
        int index = 0;

        ArrayList<Point> stepList = new ArrayList<>();

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

    private static double getSegDist(Point point, Point p1, Point p2) {

        R3 v = geoToEcef(point);
        R3 a = geoToEcef(p1);
        R3 b = geoToEcef(p2);

        return R3.distanceToSegment(v, a, b);
    }

    public static R3 geoToEcef(Point p) {
        double lat = deg2rad(p.getLat());
        double lon = deg2rad(p.getLon());
        double alt = p.getZ();
        double n = a / Math.sqrt(1 - e2 * Math.sin(lat) * Math.sin(lat));
        double x = (n + alt) * Math.cos(lat) * Math.cos(lon);    //ECEF x
        double y = (n + alt) * Math.cos(lat) * Math.sin(lon);    //ECEF y
        double z = (n * (1 - e2) + alt) * Math.sin(lat);          //ECEF z
        // fake z : increase 3x
        return new R3(x, y, 3 * z);     //Return x, y, z in ECEF
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

}
