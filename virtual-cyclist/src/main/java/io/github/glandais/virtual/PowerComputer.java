package io.github.glandais.virtual;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Formul;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKey;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.util.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.inject.Singleton;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class PowerComputer {

    // m.s-2, minimal speed = 2km/h
    private static final double MINIMAL_SPEED = 2.0 / 3.6;
    // Hz
    private static final int FREQ = 1;
    // s
    private static final double DT = 1.0 / FREQ;

    private final PowerProviderList providers;

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

        course.getGpxPath().setPoints(newPoints, ValueKind.computed);
    }

    private Point getNextPoint(Course course, CyclistStatus status) {
        final long startMillis = course.getStart().toEpochMilli();
        long now = startMillis + ((long) (1000 * status.ellapsed));
        Point current = getPoint(course, status.odo, now, ValueKind.computed);
        current.setTime(Instant.ofEpochMilli(now), ValueKind.computed);
        current.setSpeed(status.speed, ValueKind.staging);
        current.setGrade(current.getGrade(), ValueKind.staging);

        final double mKg = course.getCyclist().getMKg();
        current.putDebug("mKg", mKg, Unit.DOUBLE_ANY);

        final double crr = course.getCyclist().getCrr();
        current.putDebug("crr", crr, Unit.PERCENTAGE);

        double p_sum = 0;
        List<String> components = new ArrayList<>();
        for (PowerProvider provider : providers.getPowerProviders()) {
            double w = provider.getPowerW(course, current, status);
            if (Constants.VERIFIED) {
                current.putDebug("p_" + provider.getId() + "_verified", w, Unit.WATTS);
            }
            components.add("p_" + provider.getId());
            p_sum = p_sum + w;
        }
        current.putDebug("p_sum", new Formul(components.stream().collect(Collectors.joining("+")),
                Unit.WATTS,
                components.stream().map(c -> new ValueKey(c, ValueKind.debug)).collect(Collectors.toList())
        ), Unit.FORMULA_WATTS);

        // p_sum = 0.5 * (mKg + (0.14 / (0.7*0.7))) * (new_speed * new_speed - speed * speed) / DT
        // (new_speed * new_speed - speed * speed) = DT * p_sum / (0.5 * (mKg + (0.14 / (0.7*0.7))))
        double new_speed_squared = DT * p_sum / (0.5 * (mKg + (0.14 / (0.7 * 0.7)))) + status.speed * status.speed;
        if (new_speed_squared < 0) {
            status.speed = MINIMAL_SPEED;
        } else {
            status.speed = Math.sqrt(new_speed_squared);
        }

        String formula = "SQRT(" + DT + " * p_sum / (0.5 * (mKg + (0.14 / (0.7 * 0.7)))) + (speed) * (speed))";

        current.putDebug("new_speed", new Formul(formula,
                Unit.SPEED_S_M,
                new ValueKey("p_sum", ValueKind.debug),
                new ValueKey("mKg", ValueKind.debug),
                new ValueKey("speed", ValueKind.staging)
        ), Unit.FORMULA_SPEED_S_M);
        if (Constants.VERIFIED) {
            current.putDebug("new_speed_verified", status.speed, Unit.SPEED_S_M);
        }

        status.speed = Math.max(MINIMAL_SPEED, Math.min(current.getMaxSpeed(), status.speed));
        current.putDebug("fixed_speed", status.speed, Unit.SPEED_S_M);

        current.setSpeed(status.speed, ValueKind.computed);

        double dx = DT * status.speed;

        current.putDebug("dx", new Formul(DT + " * new_speed", Unit.METERS, new ValueKey("new_speed", ValueKind.debug)), Unit.FORMULA_METERS);
        if (Constants.VERIFIED) {
            current.putDebug("dx_verified", dx, Unit.METERS);
        }

        status.odo = status.odo + dx;
        status.ellapsed = status.ellapsed + DT;
        return current;
    }

    private Point getPoint(Course course, double d, long now, ValueKind kind) {

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
                    result.setBearing(p.getBearing(), kind);
                    return result;
                }
            }
        }
        throw new IllegalStateException();
    }

}
