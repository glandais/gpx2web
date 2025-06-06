package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.virtual.Course;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public abstract class CyclistPowerProviderBase implements CyclistPowerProvider {

    private static final double TOLERANCE = 0.05;
    private static final double MAX_MULTIPLIER = 3.0;

    private final List<Harmonic> harmonics;

    public CyclistPowerProviderBase() {
        Random r = new SecureRandom();
        harmonics = IntStream.range(0, 20)
                .mapToObj(i -> new Harmonic(r.nextDouble(1.0, 10.0), r.nextDouble(0, Math.PI), r.nextDouble(0, 0.01)))
                .toList();
    }

    protected abstract double getOptimalPower(Course course, Point location);

    @Override
    public double getPowerW(Course course, Point location) {

        double optimalPower = getOptimalPower(course, location);
        if (course.getCyclist().isHarmonics()) {
            double x = location.getInstant().toEpochMilli() / 10000.0;
            for (Harmonic harmonic : harmonics) {
                optimalPower =
                        optimalPower + harmonic.amp() * optimalPower * Math.cos(harmonic.freq() * x - harmonic.d());
            }
        }

        location.putDebug(PropertyKeys.p_cyclist_optimal_power, optimalPower);

        double optimalSpeed = course.getOptimalSpeeds().getOptimalSpeed(optimalPower, location.getGrade(), location.getBearing());
        location.putDebug(PropertyKeys.p_cyclist_optimal_speed, optimalSpeed);
        double minOptimalSpeed = optimalSpeed * (1 - TOLERANCE);
        double maxOptimalSpeed = optimalSpeed * (1 + TOLERANCE);
        double currentSpeed = location.getSpeed();
        location.putDebug(PropertyKeys.p_cyclist_current_speed, currentSpeed);
        if (minOptimalSpeed <= currentSpeed && currentSpeed <= maxOptimalSpeed) {
            return optimalPower;
        } else if (currentSpeed <= minOptimalSpeed) {
            return optimalPower * MAX_MULTIPLIER
                    - (currentSpeed / minOptimalSpeed) * optimalPower * (MAX_MULTIPLIER - 1.0);
        } else {
            double diffSpeed = currentSpeed - maxOptimalSpeed;
            double coef = Math.min(1.0, Math.max(0.0, diffSpeed / maxOptimalSpeed));
            return optimalPower - coef * optimalPower;
        }
    }
}
