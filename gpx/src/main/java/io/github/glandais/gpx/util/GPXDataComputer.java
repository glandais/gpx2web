package io.github.glandais.gpx.util;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.filter.GPXFilter;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class GPXDataComputer {

    public boolean isCrossing(GPX gpx) {
        return gpx.paths().stream().anyMatch(this::isCrossing);
    }

    public boolean isCrossing(GPXPath orig) {

        GPXPath path = new GPXPath();
        for (Point point : orig.getPoints()) {
            path.addPoint(point);
        }
        // 50m
        GPXFilter.filterPointsDouglasPeucker(path, 50);
        if (path.getPoints().size() > 2) {
            for (int i = 0; i < path.getPoints().size() - 1; i++) {
                for (int j = i + 2; j < path.getPoints().size() - 1; j++) {

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
        final Point s1p1 = path.getPoints().get(i);
        final Point s1p2 = path.getPoints().get(i + 1);

        final double x1 = s1p1.getLon();
        final double y1 = s1p1.getLat();
        final double x2 = s1p2.getLon();
        final double y2 = s1p2.getLat();

        final Point s2p1 = path.getPoints().get(j);
        final Point s2p2 = path.getPoints().get(j + 1);
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

            intersects = (ta >= 0.0f && ta <= 1.0f) && (tb >= 0.0f && tb <= 1.0f);
        }
        return intersects;
    }

    public Vector getWind(GPX gpx) {
        Vector vector = new Vector(0.0, 0.0, 0.0);
        for (GPXPath path : gpx.paths()) {
            vector = vector.add(getWindUnscaled(path));
        }
        return vector.normalize();
    }

    public Vector getWind(GPXPath path) {
        Vector windUnscaled = getWindUnscaled(path);
        return windUnscaled.normalize();
    }

    public Vector getWindUnscaled(GPXPath path) {

        final List<Point> points = path.getPoints();
        final int size = points.size();
        if (size > 3) {

            final Vector start = points.get(0).project();

            Vector tot = new Vector(0, 0, 0);
            for (Point point : points) {
                tot = tot.add(vector(start, point.project()));
            }
            tot = tot.scalar(1.0 / size);
            return tot.scalar(-1.0);
        } else {
            return new Vector(0.0, 0.0, 0.0);
        }
    }

    private Vector vector(final Vector p1, final Vector p2) {

        return new Vector(p2.x() - p1.x(), p2.y() - p1.y(), 0).normalize();
    }
}
