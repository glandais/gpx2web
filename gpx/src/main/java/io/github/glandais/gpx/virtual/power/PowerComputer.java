package io.github.glandais.gpx.virtual.power;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.virtual.Constants;
import io.github.glandais.gpx.virtual.Course;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class PowerComputer {

    private final PowerProviderList providers;

    public double getNewSpeedAfterDt(Course course, Point current) {
        double pSum = getNewPower(course, current, true);

        double currentSpeed = current.getSpeed();
        double mKg = course.getCyclist().getMKg();
        double inertiaFront = course.getBike().getInertiaFront();
        double inertiaRear = course.getBike().getInertiaRear();
        double wheelRadius = course.getBike().getWheelRadius();
        // p_sum = 0.5 * (mKg + ((I1 + I2) / (r^2))) * (new_speed * new_speed - speed * speed) / DT
        // (new_speed * new_speed - speed * speed) = DT * p_sum / (0.5 * (mKg + ((I1 + I2) / (r^2))))
        double inertia = inertiaFront + inertiaRear;
        double new_speed_squared = Constants.DT * pSum / (0.5 * (mKg + (inertia / (wheelRadius * wheelRadius)))) + currentSpeed * currentSpeed;

        if (new_speed_squared < Constants.MINIMAL_SPEED * Constants.MINIMAL_SPEED) {
            return Constants.MINIMAL_SPEED;
        } else {
            return Math.sqrt(new_speed_squared);
        }
    }

    private double getNewPower(Course course, Point current, boolean withCyclist) {
        double pSum = 0;
        String suffix = withCyclist ? "" : "recompute_";
        for (PowerProvider provider : providers.getPowerProviders()) {
            if (withCyclist || provider.getId() != PowerProviderId.cyclist) {
                double w = provider.getPowerW(course, current);
                current.putDebug("p_" + suffix + provider.getId(), w, Unit.WATTS);
                pSum = pSum + w;
            }
        }
        current.putDebug("p_" + suffix + "sum", pSum, Unit.WATTS);

        return pSum;
    }

    public double computeCyclistPower(Course course, Point p1, Point p2) {
        double power = getNewPower(course, p1, false);
        double s1 = p1.getSpeed();
        double s2 = p2.getSpeed();
        double mKg = course.getCyclist().getMKg();
        double inertiaFront = course.getBike().getInertiaFront();
        double inertiaRear = course.getBike().getInertiaRear();
        double inertia = inertiaFront + inertiaRear;
        double wheelRadius = course.getBike().getWheelRadius();
        double dt = getDt(p1, p2);
        // p_sum = 0.5 * (mKg + ((I1 + I2) / (r^2))) * (new_speed * new_speed - speed * speed) / DT
        double tot_power = 0.5 * (mKg + (inertia / (wheelRadius * wheelRadius))) * (s2 * s2 - s1 * s1) / dt;
        p1.putDebug("p_power_from_acc", tot_power, Unit.WATTS);
        double cyclistPower = tot_power - power;
        p1.putDebug("p_power_wheel_from_acc", cyclistPower, Unit.WATTS);
        cyclistPower = Math.min(1000, Math.max(0.0, cyclistPower));
        cyclistPower = cyclistPower / course.getBike().getEfficiency();
        return cyclistPower;
    }

    private double getDt(Point p1, Point p2) {
        Duration duration = Duration.between(p1.getInstant(), p2.getInstant());

        long seconds = duration.getSeconds();
        int nanoAdjustment = duration.getNano();

        return seconds + (nanoAdjustment / 1_000_000_000.0);
    }
}
