package io.github.glandais.gpx.virtual.power;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.virtual.Constants;
import io.github.glandais.gpx.virtual.Course;
import jakarta.inject.Singleton;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class PowerComputer {

    private final PowerProviderList providers;

    public double getNewPower(Course course, Point current, boolean withCyclist) {
        double pSum = 0;
        for (PowerProvider provider : providers.getPowerProviders()) {
            if (withCyclist || provider.getId() != PowerProviderId.cyclist) {
                double w = provider.getPowerW(course, current);
                pSum = pSum + w;
            }
        }
        return pSum;
    }

    public double getDx(double pSum, double equivalentMass, double currentSpeed, double dt) {
        // p_sum = 0.5 * (mKg + ((I1 + I2) / (r^2))) * (new_speed * new_speed - speed * speed) / DT
        // (new_speed * new_speed - speed * speed) = DT * p_sum / (0.5 * (mKg + ((I1 + I2) / (r^2))))
        double newSpeed = Math.max(
                Math.sqrt(dt * pSum / (0.5 * equivalentMass) + currentSpeed * currentSpeed), Constants.MINIMAL_SPEED);
        return (newSpeed + currentSpeed) * dt / 2;
    }

    public double getDt(double pSum, double equivalentMass, double currentSpeed, double dx) {
        double dt1 = -0.1;
        double dt2 = Constants.DT + 0.1;

        while (dt2 - dt1 >= dx / 10_000_000.0) {
            double dtMiddle = (dt1 + dt2) / 2;
            double dxMiddle = getDx(pSum, equivalentMass, currentSpeed, dtMiddle);

            if (dxMiddle < dx) {
                dt1 = dtMiddle;
            } else {
                dt2 = dtMiddle;
            }
        }

        return (dt1 + dt2) / 2;
    }

    public double computeCyclistPower(Course course, double equivalentMass, Point p1, Point p2) {
        double power = getNewPower(course, p1, false);
        double s1 = p1.getSpeed();
        double s2 = p2.getSpeed();
        double dt = getDt(p1, p2);

        // p_sum = 0.5 * (mKg + ((I1 + I2) / (r^2))) * (new_speed * new_speed - speed * speed) / DT
        double tot_power = getTotPower(equivalentMass, s1, s2, dt);
        p1.putDebug(PropertyKeys.p_power_from_acc, tot_power);
        double cyclistPower = tot_power - power;
        p1.putDebug(PropertyKeys.p_power_wheel_from_acc, cyclistPower);
        cyclistPower = Math.max(0.0, cyclistPower);
        cyclistPower = cyclistPower / course.getBike().getEfficiency();
        return cyclistPower;
    }

    public double getTotPower(double equivalentMass, double s1, double s2, double dt) {
        return 0.5 * equivalentMass * (s2 * s2 - s1 * s1) / dt;
    }

    private double getDt(Point p1, Point p2) {
        Duration duration = Duration.between(p1.getInstant(), p2.getInstant());
        return duration.toNanos() / 1_000_000_000.0;
    }

    public double getEquivalentMass(Course course) {
        double mKg = course.getCyclist().getMKg();
        double inertiaFront = course.getBike().getInertiaFront();
        double inertiaRear = course.getBike().getInertiaRear();
        double wheelRadius = course.getBike().getWheelRadius();
        double inertia = inertiaFront + inertiaRear;
        return mKg + (inertia / (wheelRadius * wheelRadius));
    }
}
