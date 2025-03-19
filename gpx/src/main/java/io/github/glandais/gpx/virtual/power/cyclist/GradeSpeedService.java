package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.power.PowerProvider;
import io.github.glandais.gpx.virtual.power.aero.AeroPowerProvider;
import io.github.glandais.gpx.virtual.power.grav.GravPowerProvider;
import io.github.glandais.gpx.virtual.power.rolling.RollingResistancePowerProvider;
import io.github.glandais.gpx.virtual.power.rolling.WheelBearingsPowerProvider;
import jakarta.inject.Singleton;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class GradeSpeedService {

    final WheelBearingsPowerProvider wheelBearingsPowerProvider;

    final RollingResistancePowerProvider rollingResistancePowerProvider;

    final AeroPowerProvider aeroPowerProvider;

    final GravPowerProvider gravPowerProvider;

    public double getSpeed(Course course, double grade, double power) {
        Point p = new Point();
        p.setGrade(grade);
        return getSpeed(p, course, power, 0.1, 30);
    }

    private double getSpeed(Point p, Course course, double power, double min, double max) {
        double average = (min + max) / 2;
        if (Math.abs(min - max) < 0.1) {
            return average;
        }
        p.setSpeed(average);
        double pSum = 0.0;
        for (PowerProvider powerProvider : getPowerProviders()) {
            pSum = pSum + powerProvider.getPowerW(course, p);
        }
        if (-pSum > power) {
            return getSpeed(p, course, power, min, average);
        } else {
            return getSpeed(p, course, power, average, max);
        }
    }

    protected List<PowerProvider> getPowerProviders() {
        return List.of(
                wheelBearingsPowerProvider, rollingResistancePowerProvider, gravPowerProvider, aeroPowerProvider);
    }
}
