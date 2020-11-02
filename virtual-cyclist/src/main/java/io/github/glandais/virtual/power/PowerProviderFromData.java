package io.github.glandais.virtual.power;

import io.github.glandais.gpx.Point;
import lombok.AllArgsConstructor;

import java.time.Duration;
import java.time.ZonedDateTime;

@AllArgsConstructor
public class PowerProviderFromData implements PowerProvider {

    @Override
    public double getPowerW(Point from, Point to, ZonedDateTime now, Duration ellapsed, double p_air, double p_frot, double p_grav, double v, double grad) {

        Double p = from.getData()
                .get("power");
        if (p == null) {
            return 0;
        }
        return p;
    }
}
