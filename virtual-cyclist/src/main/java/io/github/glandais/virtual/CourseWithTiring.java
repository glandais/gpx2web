package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CourseWithTiring extends Course {

    private final long duration;

    public CourseWithTiring(GPXPath gpxPath, Cyclist cyclist, ZonedDateTime start, double windSpeed, double windDirection, long duration) {
        super(gpxPath, cyclist, start, windSpeed, windDirection);
        this.duration = duration;
    }

    public double getPowerW(Point from, Point to, long currentTime, double p_air, double p_frot, double p_grav, double v, double grad) {

        double powerW = super.getPowerW(from, to, currentTime, p_air, p_frot, p_grav, v, grad);
        double c = Math.max(0.5, 1 - (0.6 * currentTime / duration));
        return powerW * c;
    }

}
