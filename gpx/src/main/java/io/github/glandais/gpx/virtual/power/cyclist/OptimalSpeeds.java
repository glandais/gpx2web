package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.virtual.Course;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class OptimalSpeeds {

    static class Ratio {
        final double min;
        final double max;
        final int count;
        final double ratio;

        Ratio(double min, double max, int count) {
            this.min = min;
            this.max = max;
            this.count = count;
            this.ratio = (max - min) / count;
        }

        double getValue(int i) {
            return this.min + i * ratio;
        }

        int getIndex(double value) {
            value = Math.max(min, Math.min(max, value));
            return (int) ((value - min) / ratio);
        }

        void forValues(BiConsumer<Integer, Double> consumer) {
            for (int i = 0; i <= count; i++) {
                consumer.accept(i, getValue(i));
            }
        }
    }

    private static final Ratio bearingRatio = new Ratio(0, Math.PI, 12);
    private static final Ratio powerRatio = new Ratio(0, 1000, 100);
    private static final Ratio gradeRatio = new Ratio(-0.2, 0.2, 400);

    // bearing -> power -> grade -> speed
    private final Map<Integer, Map<Integer, Map<Integer, Double>>> speeds;

    public OptimalSpeeds(OptimalSpeedService optimalSpeedService, Course course) {
        super();
        this.speeds = new HashMap<>();
        bearingRatio.forValues((i, bearing) -> speeds.put(i, getOptimalSpeeds(course, bearing, optimalSpeedService)));
    }

    private Map<Integer, Map<Integer, Double>> getOptimalSpeeds(
            Course course, double bearing, OptimalSpeedService optimalSpeedService) {
        Map<Integer, Map<Integer, Double>> speeds = new HashMap<>();
        powerRatio.forValues(
                (i, power) -> speeds.put(i, getOptimalSpeeds(course, bearing, power, optimalSpeedService)));
        return speeds;
    }

    private Map<Integer, Double> getOptimalSpeeds(
            Course course, double bearing, double power, OptimalSpeedService optimalSpeedService) {
        Map<Integer, Double> speeds = new HashMap<>();
        gradeRatio.forValues((i, grade) -> speeds.put(i, optimalSpeedService.getSpeed(course, grade, power, bearing)));
        return speeds;
    }

    public double getOptimalSpeed(double power, double grade, double bearing) {
        return speeds.get(bearingRatio.getIndex(bearing))
                .get(powerRatio.getIndex(power))
                .get(gradeRatio.getIndex(grade));
    }
}
