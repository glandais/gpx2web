package io.github.glandais.virtual.cyclist;

import io.github.glandais.gpx.Point;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PowerProviderConstant implements PowerProvider {

    private final double power;

    public PowerProviderConstant() {
        this(280);
    }

    @Override
    public String getId() {
        return "constant";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {

        double grade = location.getGrade();
        if (grade < -0.06) {
            return 0;
        } else if (grade < 0) {
            // -6% : 0%
            // 0% : 100%
            double c = 1 + (grade / 0.06);
            return power * c;
        } else {
            return power;
        }
    }
}
