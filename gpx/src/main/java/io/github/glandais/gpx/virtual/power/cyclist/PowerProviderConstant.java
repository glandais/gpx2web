package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.virtual.Course;

import java.security.SecureRandom;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class PowerProviderConstant implements CyclistPowerProvider {

    private static final double TOLERANCE = 0.05;
    private static final double MAX_MULTIPLIER = 3.0;
    private static final double CUT_OFF = 2.0;

    private final List<Harmonic> harmonics;

    public PowerProviderConstant() {
        Random r = new SecureRandom();
        harmonics = IntStream.range(0, 20)
                .mapToObj(i -> new Harmonic(
                        r.nextDouble(1.0, 10.0),
                        r.nextDouble(0, Math.PI),
                        r.nextDouble(0, 0.01)
                ))
                .toList();
    }

    @Override
    public double getPowerW(Course course, Point location) {

        double optimalPower = course.getCyclist().getPower();
        double x = location.getInstant().toEpochMilli() / 10000.0;
        for (Harmonic harmonic : harmonics) {
            optimalPower = optimalPower + harmonic.amp() * optimalPower * Math.cos(harmonic.freq() * x - harmonic.d());
        }

        location.putDebug("p_cyclist_optimal_power", optimalPower, Unit.WATTS);

        double grade = location.getGrade();
        if (grade < -0.20) {
            return 0.0;
        } else if (grade > 0.20) {
            return course.getCyclist().getPower();
        }

        double optimalSpeed = course.getGradeSpeeds().getOptimalSpeed(100.0 * location.getGrade());
        location.putDebug("p_cyclist_optimal_speed", optimalSpeed, Unit.SPEED_S_M);
        double minOptimalSpeed = optimalSpeed * (1 - TOLERANCE);
        double maxOptimalSpeed = optimalSpeed * (1 + TOLERANCE);
        double currentSpeed = location.getSpeed();
        location.putDebug("p_cyclist_current_speed", currentSpeed, Unit.SPEED_S_M);
        if (minOptimalSpeed <= currentSpeed && currentSpeed <= maxOptimalSpeed) {
            return optimalPower;
        } else if (currentSpeed <= minOptimalSpeed) {
            return optimalPower * MAX_MULTIPLIER - (currentSpeed / minOptimalSpeed) * optimalPower * (MAX_MULTIPLIER - 1.0);
        } else {
            double diffSpeed = currentSpeed - maxOptimalSpeed;
            double coef = Math.min(1.0, Math.max(0.0, diffSpeed / maxOptimalSpeed));
            return optimalPower - coef * optimalPower;
        }
    }

}
