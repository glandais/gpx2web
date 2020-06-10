package io.github.glandais.gpx;

import io.github.glandais.gpx.geocalc.Coordinate;
import io.github.glandais.gpx.geocalc.EarthCalc;
import io.github.glandais.gpx.geocalc.GeocalcPoint;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class GPXDataComputer {

    public boolean isCrossing(GPXPath path) {

        if (path.getPoints()
                .size() > 2) {

            for (int i = 0;
                    i < path.getPoints()
                            .size() - 1;
                    i++) {
                for (int j = i + 2;
                        j < path.getPoints()
                                .size() - 1;
                        j++) {

                    if (isIntersects(path, i, j)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isIntersects(final GPXPath path, final int i, final int j) {

        final boolean intersects;
        final Point s1p1 = path.getPoints()
                .get(i);
        final Point s1p2 = path.getPoints()
                .get(i + 1);

        final double x1 = s1p1.getLon();
        final double y1 = s1p1.getLat();
        final double x2 = s1p2.getLon();
        final double y2 = s1p2.getLat();

        final Point s2p1 = path.getPoints()
                .get(j);
        final Point s2p2 = path.getPoints()
                .get(j + 1);
        final double x3 = s2p1.getLon();
        final double y3 = s2p1.getLat();
        final double x4 = s2p2.getLon();
        final double y4 = s2p2.getLat();

        final double v = (x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3);
        if (v == 0) {
            intersects = false;
        } else {

            final double ta = ((y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)) / v;
            final double tb = ((y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)) / v;

            if ((ta >= 0.0f && ta <= 1.0f) && (tb >= 0.0f && tb <= 1.0f)) {

                intersects = true;
            } else {
                intersects = false;
            }
        }
        return intersects;
    }

    public double getDirection(GPXPath path) {

        if (path.getPoints()
                .size() > 1) {
            final Point start = path.getPoints()
                    .get(0);
            final GeocalcPoint geoStart = GeocalcPoint.at(Coordinate.fromDegrees(start.getLat()), Coordinate.fromDegrees(start.getLon()));
            double bearing = 0;
            int nBearing = 0;
            for (int i = 1;
                    i < path.getPoints()
                            .size();
                    i++) {
                final Point point = path.getPoints()
                        .get(i);
                final GeocalcPoint geoPoint =
                        GeocalcPoint.at(Coordinate.fromDegrees(point.getLat()), Coordinate.fromDegrees(point.getLon()));

                bearing = bearing + EarthCalc.bearing(geoStart, geoPoint);
                nBearing++;

            }
            bearing = bearing / (1.0 * nBearing);
            return bearing;
        } else {
            return 0.0;
        }
    }

}
