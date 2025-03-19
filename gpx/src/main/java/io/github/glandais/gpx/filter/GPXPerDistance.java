package io.github.glandais.gpx.filter;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Singleton
@Slf4j
public class GPXPerDistance {

    public void computeOnePointPerDistance(GPXPath path, double minDist) {
        log.debug("A point per distant for {} {}", path.getName(), minDist);
        List<Point> points = path.getPoints();
        List<Point> newPoints = new ArrayList<>();

        for (int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);
            newPoints.add(p);
            Point pp1 = points.get(i + 1);
            double dist = p.distanceTo(pp1);
            if (dist > minDist) {
                double n = Math.ceil(dist / minDist);
                for (int j = 1; j < n; j++) {
                    double c = j / n;
                    Point point = Point.interpolate(p, pp1, c);
                    newPoints.add(point);
                }
            }
        }
        newPoints.add(points.get(points.size() - 1));

        path.setPoints(newPoints);
        log.debug("Done - a point per distant for {} {}", path.getName(), minDist);
    }
}
