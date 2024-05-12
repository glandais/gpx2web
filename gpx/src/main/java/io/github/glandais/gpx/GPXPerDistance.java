package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.ValueKind;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Service
@Singleton
@Slf4j
public class GPXPerDistance {

    public void computeOnePointPerDistance(GPXPath path, double minDist) {
        log.info("A point per distant for {} {}", path.getName(), minDist);
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

        path.setPoints(newPoints, ValueKind.computed);
        log.info("Done - a point per distant for {} {}", path.getName(), minDist);
    }

}
