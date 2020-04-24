package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.ZonedDateTime;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class CourseWithPower extends Course {

    public CourseWithPower(GPXPath original, Cyclist cyclist, ZonedDateTime date) {

        super(original, cyclist, date);
    }

    @Override
    public double getPowerW(Point from, Point to, double p_air, double p_frot, double p_grav, double v, double grad) {

        Double p = from.getData()
                .get("power");
        if (p == null) {
            return 0;
        }
        return p;
    }
}
