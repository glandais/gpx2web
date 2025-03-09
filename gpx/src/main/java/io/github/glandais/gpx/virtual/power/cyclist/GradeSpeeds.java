package io.github.glandais.gpx.virtual.power.cyclist;

import io.github.glandais.gpx.virtual.Course;

import java.util.HashMap;
import java.util.Map;

public class GradeSpeeds {

    private final Map<Integer, Double> speeds;

    public GradeSpeeds(GradeSpeedService gradeSpeedService, Course course) {
        super();
        speeds = new HashMap<>();
        double power = course.getCyclist().getPower();
        for (int i = -200; i <= 200; i++) {
            speeds.put(i + 200, gradeSpeedService.getSpeed(course, i / 1000.0, power));
        }
    }

    public double getOptimalSpeed(double grade) {
        return speeds.get((int) (grade * 10) + 200);
    }

}
