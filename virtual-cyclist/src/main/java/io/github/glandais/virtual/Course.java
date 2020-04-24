package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

import java.time.ZonedDateTime;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor
@ToString
@EqualsAndHashCode
public class Course {

    private final GPXPath gpxPath;

    private final Cyclist cyclist;

    private final ZonedDateTime start;

    public double getPowerW(Point from, Point to, double p_air, double p_frot, double p_grav, double v, double grad) {

        if (grad < -0.06) {
            return 0;
        } else if (grad < 0) {
            double c = 1 - (grad / -0.06);
            return cyclist.getPowerW() * c;
        } else {
            return cyclist.getPowerW();
        }
    }

}
