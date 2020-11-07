package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;

import java.time.Instant;

public class PowerProviderConstantWithTiring extends PowerProviderConstant {

    private final double duration;

    public PowerProviderConstantWithTiring(double power, double duration) {
        super(power);
        this.duration = duration;
    }

    @Override
    public double getPowerW(Point location, double ellapsed, double p_air, double p_frot, double p_grav, double v, double grad) {

        double powerW = super.getPowerW(location, ellapsed, p_air, p_frot, p_grav, v, grad);
        double c = Math.max(0.5, 1 - (0.6 * ellapsed / duration));
        return powerW * c;
    }
}
