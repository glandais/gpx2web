package io.github.glandais.gpx.filter;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.util.Simplifier;
import io.github.glandais.gpx.util.Vector;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GPXFilter {

    private static final double TOLERANCE = 3.0;

    private static final Simplifier<Point> simplifier = new Simplifier<>(GPXFilter::geoToEcef);

    public static void filterPointsDouglasPeucker(GPXPath path) {
        filterPointsDouglasPeucker(path, TOLERANCE);
    }

    public static void filterPointsDouglasPeucker(GPXPath path, double tolerance) {

        List<Point> points = path.getPoints();

        if (points.size() <= 2) {
            return;
        }

        log.debug("Filtering {} ({})", path.getName(), points.size());

        List<Point> newPoints = simplifier.douglasPeucker(points, tolerance);
        path.setPoints(newPoints);
        log.debug("Filtered {} ({} -> {})", path.getName(), points.size(), newPoints.size());
    }

    public static Vector geoToEcef(Point p) {
        double lat = p.getLat();
        double lon = p.getLon();
        // fake z : increase 3x
        double ele = 3 * p.getEle();
        double n = Constants.SEMI_MAJOR_AXIS
                / Math.sqrt(1 - Constants.FIRST_ECCENTRICITY_SQUARED * Math.sin(lat) * Math.sin(lat));
        double x = (n + ele) * Math.cos(lat) * Math.cos(lon); // ECEF x
        double y = (n + ele) * Math.cos(lat) * Math.sin(lon); // ECEF y
        double z = (n * (1 - Constants.FIRST_ECCENTRICITY_SQUARED) + ele) * Math.sin(lat); // ECEF z
        return new Vector(x, y, z); // Return x, y, z in ECEF
    }
}
