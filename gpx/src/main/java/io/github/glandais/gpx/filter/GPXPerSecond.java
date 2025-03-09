package io.github.glandais.gpx.filter;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.ValueKind;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Singleton
@Slf4j
public class GPXPerSecond {

    public void computeOnePointPerSecond(GPXPath path) {
        log.debug("A point per second for {}", path.getName());
        List<Point> points = path.getPoints();
        List<Point> newPoints = new ArrayList<>();

        long[] time = path.getTime();
        long s = 1000 * (long) Math.ceil(time[0] / 1000.0);
        long e = 1000 * (long) Math.floor(time[time.length - 1] / 1000.0);
        int i = 0;

        while (s <= e) {
            while ((i + 1) < time.length && time[i + 1] < s) {
                i++;
            }
            if ((i + 1) < time.length) {
                if (time[i] <= s && s <= time[i + 1]) {
                    Point p = points.get(i);
                    if (time[i + 1] - time[i] > 1) {
                        double c = (s - time[i]) / (1.0 * time[i + 1] - time[i]);
                        Point pp1 = points.get(i + 1);

                        Point point = Point.interpolate(p, pp1, c, s);
                        newPoints.add(point);
                    } else {
                        newPoints.add(p);
                    }
                } else {
                    log.error("strange");
                }
            }
            s = s + 1000;
        }
        Point end = points.get(points.size() - 1).copy();
        end.setInstant(Instant.ofEpochMilli(e + 1000), ValueKind.computed);
        newPoints.add(end);

        path.setPoints(newPoints, ValueKind.computed);
        log.debug("Done - a point per second for {}", path.getName());
    }

}
