package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class PowerProviderConstant implements PowerProvider {

    private final double power;

    public PowerProviderConstant() {
        this(280);
    }

    @Override
    public double getPowerW(Point from, Point to, ZonedDateTime now, Duration ellapsed, double p_air, double p_frot, double p_grav, double v, double grad) {

        if (grad < -0.06) {
            return 0;
        } else if (grad < 0) {
            // -6% : 0%
            // 0% : 100%
            double c = 1 + (grad / 0.06);
            return power * c;
        } else {
            return power;
        }
    }
}
