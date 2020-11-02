package io.github.glandais.gpx;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
public class SimpleTimeComputer {

    /**
     * Compute time on path with simple speed.
     *
     * @param path  the path
     * @param start the start
     * @param speed the speed in m.s-2
     */
    public void computeTime(GPXPath path, ZonedDateTime start, double speed) {
        for (Point point : path.getPoints()) {
            long time = Math.round(1000 * point.getDist() / speed);
            point.setTime(start.plus(Duration.ofMillis(time)));
        }
        path.computeArrays();
    }

}
