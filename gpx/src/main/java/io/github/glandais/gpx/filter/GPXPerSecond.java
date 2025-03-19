package io.github.glandais.gpx.filter;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Singleton
@Slf4j
public class GPXPerSecond {

    public void computeOnePointPerSecond(GPXPath path) {
        log.debug("A point per second for {}", path.getName());
        List<Point> points = path.getPoints();
        Map<Long, Point> newPoints = new TreeMap<>();

        for (int i = 0; i < points.size(); i++) {
            Point point1 = points.get(i);
            Instant instant1 = point1.getInstant();

            // first point
            if (i == 0) {
                // not on an epoch second
                if (instant1.getNano() != 0) {
                    // copy
                    Point start = point1.copy();
                    // add at start of second
                    addPoint(newPoints, start, instant1.getEpochSecond());
                }
            }

            // last point
            if (i == points.size() - 1) {
                // not on an epoch second
                if (instant1.getNano() != 0) {
                    // copy
                    Point end = point1.copy();
                    // add at end of second
                    addPoint(newPoints, end, instant1.getEpochSecond() + 1);
                }
            } else {
                Point point2 = points.get(i + 1);
                Instant instant2 = point2.getInstant();
                if (instant1.getEpochSecond() != instant2.getEpochSecond()) {
                    long nano12 = Duration.between(instant1, instant2).toNanos();

                    long epochStart =
                            instant1.getNano() == 0 ? instant1.getEpochSecond() : instant1.getEpochSecond() + 1;
                    long epochEnd = instant2.getEpochSecond();
                    for (long epoch = epochStart; epoch <= epochEnd; epoch++) {
                        Instant instant = Instant.ofEpochSecond(epoch);
                        long nanoToEpoch = Duration.between(instant1, instant).toNanos();

                        double c = 1.0 * nanoToEpoch / nano12;

                        Point point = Point.interpolate(point1, point2, c);
                        addPoint(newPoints, point, epoch);
                    }
                }
            }
        }

        path.setPoints(new ArrayList<>(newPoints.values()));
        log.debug("Done - a point per second for {}", path.getName());
    }

    private void addPoint(Map<Long, Point> newPoints, Point point, long epochSeconds) {
        point.setInstant(null, Instant.ofEpochSecond(epochSeconds));
        newPoints.put(epochSeconds, point);
    }
}
