package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;

import java.time.Duration;
import java.time.ZonedDateTime;

public class PowerProviderConstantWithTiring extends PowerProviderConstant {

    private final long durationMillis;

    public PowerProviderConstantWithTiring(double power, long durationMillis) {
        super(power);
        this.durationMillis = durationMillis;
    }

    @Override
    public double getPowerW(Point from, Point to, ZonedDateTime now, Duration ellapsed, double p_air, double p_frot, double p_grav, double v, double grad) {

        double powerW = super.getPowerW(from, to, now, ellapsed, p_air, p_frot, p_grav, v, grad);
        double c = Math.max(0.5, 1 - (0.6 * ellapsed.toMillis() / durationMillis));
        return powerW * c;
    }
}
