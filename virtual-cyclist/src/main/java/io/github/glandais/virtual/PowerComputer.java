package io.github.glandais.virtual;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PowerComputer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputer.class);

    // m.s-2, minimal speed = 2km/h
    private static final double MINIMAL_SPEED = 2.0 / 3.6;
    // Hz
    private static final int FREQ = 4;
    // s
    private static final double DT = 1.0 / FREQ;

    private final List<PowerProvider> providers;

    public PowerComputer(List<PowerProvider> providers) {
        super();
        this.providers = providers;
    }

    public void computeTrack(Course course) {

        final List<Point> newPoints = new ArrayList<>();

        final CyclistStatus status = new CyclistStatus();
        status.speed = MINIMAL_SPEED;

        int i = 0;
        Point nextPoint = null;
        while (status.odo < course.getGpxPath().getDist()) {
            nextPoint = getNextPoint(course, status);
            if (i % FREQ == 0) {
                newPoints.add(nextPoint);
            }
            i++;
        }
        if ((i - 1) % FREQ == 0) {
            newPoints.add(nextPoint);
        }

        course.getGpxPath().setPoints(newPoints);
        course.getGpxPath().computeArrays();
    }

    private Point getNextPoint(Course course, CyclistStatus status) {
        final long startMillis = course.getStart().toEpochMilli();
        long now = startMillis + ((long) (1000 * status.ellapsed));
        Point current = getPoint(course, status.odo, now);
        current.setTime(Instant.ofEpochMilli(now));

        current.putDebug("0_0_odo", status.odo, Unit.METERS);
        current.putDebug("0_1_speed", status.speed, Unit.SPEED_S_M);

        double p_app = 0;
        int i = 0;
        for (PowerProvider provider : providers) {
            double w = provider.getPowerW(course, current, status);
            current.putDebug("1_" + i++ + "_power", w, Unit.WATTS);
            p_app = p_app + w;
        }

        // p_app = cyclist power - resistance
        current.putDebug("5_1_p_app", p_app, Unit.WATTS);

        final double mKg = course.getCyclist().getMKg();
        // m.s-2
        double acc = p_app / (Constants.G * mKg);
        current.putDebug("5_2_acc", acc, Unit.DOUBLE_ANY);

        status.speed = status.speed + acc * DT;
        current.putDebug("5_3_new_speed", status.speed, Unit.SPEED_S_M);

        current.putDebug("5_4_max_speed", current.getMaxSpeed(), Unit.SPEED_S_M);

        status.speed = Math.max(MINIMAL_SPEED, Math.min(current.getMaxSpeed(), status.speed));
        current.putDebug("5_5_fixed_speed", status.speed, Unit.SPEED_S_M);

        current.setSpeed(status.speed);

        double dx = DT * status.speed;
        current.putDebug("5_6_dx", dx, Unit.METERS);

        status.odo = status.odo + dx;
        status.ellapsed = status.ellapsed + DT;
        //log.info("{}s {}m", ellapsed, odo);
        return current;
    }

    private Point getPoint(Course course, double d, long now) {

        List<Point> points = course.getGpxPath().getPoints();
        double[] dists = course.getGpxPath().getDists();
        for (int i = 0; i < points.size(); i++) {
            if (d <= dists[i]) {
                if (i == 0) {
                    return points.get(0);
                } else {
                    Point p = points.get(i - 1);
                    Point pp1 = points.get(i);
                    if (dists[i] - dists[i - 1] == 0) {
                        continue;
                    }

                    double ratio = (d - dists[i - 1]) / (dists[i] - dists[i - 1]);
                    Point result = Point.interpolate(p, pp1, ratio, now);
                    result.setGrade(p.getGrade());
                    result.setBearing(p.getBearing());
                    return result;
                }
            }
        }
        throw new IllegalStateException();
    }

}
