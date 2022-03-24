package io.github.glandais.gpx;

import io.github.glandais.gpx.storage.ValueKind;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.time.Instant;

@Service
@Singleton
public class SimpleTimeComputer {

    /**
     * Compute time on path with simple speed.
     *
     * @param path  the path
     * @param start the start
     * @param speed the speed in m.s-2
     */
    public void computeTime(GPXPath path, Instant start, double speed) {
        for (Point point : path.getPoints()) {
            long time = Math.round(1000 * point.getDist() / speed);
            point.setTime(start.plusMillis(time), ValueKind.computed);
        }
        path.computeArrays(ValueKind.computed);
    }

}
