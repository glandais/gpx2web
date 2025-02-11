package io.github.glandais.virtual;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Formul;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.util.Constants;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    // s
    private static final double DT = 1.0;

    private final PowerProviderList providers;

    public void computeTrack(Course course) {

        course.getGpxPath().computeArrays(ValueKind.computed);
        final List<Point> newPoints = new ArrayList<>();

        final CyclistStatus status = new CyclistStatus();

        while (status.odo < course.getGpxPath().getDist()) {
            newPoints.add(getNextPoint(course, status));
        }

        course.getGpxPath().setPoints(newPoints, ValueKind.computed);
    }

    private Point getNextPoint(Course course, CyclistStatus status) {
        final long startMillis = course.getStart().toEpochMilli();
        long now = startMillis + ((long) (1000 * status.ellapsed));

        Point current = getPoint(course, status.odo, now, ValueKind.computed);
        current.setTime(Instant.ofEpochMilli(now), ValueKind.computed);
        current.setSpeed(status.speed, ValueKind.staging);
        double speedOld = status.speed;
        current.putDebug("speed_old", status.speed, Unit.SPEED_S_M);
        current.setGrade(current.getGrade(), ValueKind.staging);

        final double mKg = course.getCyclist().mKg();
        current.putDebug("mKg", mKg, Unit.DOUBLE_ANY);

        final double crr = course.getBike().crr();
        current.putDebug("crr", crr, Unit.PERCENTAGE);

        double inertiaFront = course.getBike().inertiaFront();
        double inertiaRear = course.getBike().inertiaRear();
        double wheelRadius = course.getBike().wheelRadius();

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
        current.putDebug("p_sum_verified", p_sum, Unit.WATTS);


        // p_sum = 0.5 * (mKg + ((I1 + I2) / (r^2))) * (new_speed * new_speed - speed * speed) / DT
        // (new_speed * new_speed - speed * speed) = DT * p_sum / (0.5 * (mKg + ((I1 + I2) / (r^2))))
        double inertia = inertiaFront + inertiaRear;
        double new_speed_squared = DT * p_sum / (0.5 * (mKg + (inertia / (wheelRadius * wheelRadius)))) + status.speed * status.speed;

        double speedNew;
        if (new_speed_squared < MINIMAL_SPEED * MINIMAL_SPEED) {
            speedNew = MINIMAL_SPEED;
        } else {
            speedNew = Math.sqrt(new_speed_squared);
        }

        String formula = "SQRT(" + DT + " * p_sum / (0.5 * (mKg + (" + inertia + " / (" + wheelRadius + " * " + wheelRadius + ")))) + (speed) * (speed))";
        current.putDebug("speed_new", new Formul(formula,
                Unit.SPEED_S_M,
                new ValueKey("p_sum", ValueKind.debug),
                new ValueKey("mKg", ValueKind.debug),
                new ValueKey("speed", ValueKind.staging)
        ), Unit.FORMULA_SPEED_S_M);

        double dx;
        double idspeed = 0;
        Point next;
        do {
            status.speed = speedNew - idspeed * (0.5 / 3.6);
            dx = DT * (speedOld + status.speed) / 2;
            next = getPoint(course, status.odo + dx, now, ValueKind.computed);
            idspeed++;
        } while (status.speed > next.getSpeedMax() && status.speed > MINIMAL_SPEED);
        if (status.speed < MINIMAL_SPEED) {
            status.speed = MINIMAL_SPEED;
        }

        current.putDebug("speed_new", status.speed, Unit.SPEED_S_M);
        current.putDebug("dx", new Formul(DT + " * (speed_new + speed_old) / 2", Unit.METERS, new ValueKey("speed_new", ValueKind.debug), new ValueKey("speed_old", ValueKind.debug)), Unit.FORMULA_METERS);
        if (Constants.VERIFIED) {
            current.putDebug("dx_verified", dx, Unit.METERS);
        }
        current.putDebug("speed_real", dx / DT, Unit.SPEED_S_M);

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

                    double dx = d - dists[i - 1];
                    double ratio = dx / (dists[i] - dists[i - 1]);
                    Point result = Point.interpolate(p, pp1, ratio, now);
                    result.setBearing(p.getBearing(), kind);
                    return result;
                }
            }
        }
        return points.get(points.size() - 1);
    }

}
