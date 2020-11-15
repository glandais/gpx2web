package io.github.glandais.virtual.rolling;

import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.PowerProvider;
import org.springframework.stereotype.Service;

@Service
public class WheelBearingsPowerProvider implements PowerProvider {
    @Override
    public String getId() {
        return "bearings";
    }

    @Override
    public double getPowerW(Course course, Point location, CyclistStatus status) {
        double p_wb = -status.getSpeed() * (91 + 8.7 * status.getSpeed()) / 1000.0;
        location.putDebug("3_1_p_wb", p_wb, Unit.WATTS);
        return p_wb;
    }

}
