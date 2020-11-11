package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import io.github.glandais.virtual.frot.FrotPowerProvider;
import io.github.glandais.virtual.grav.GravPowerProvider;
import io.github.glandais.virtual.wind.WindProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CxGuesser {

    private final FrotPowerProvider frotPowerProvider;

    private final GravPowerProvider gravPowerProvider;

    public CxGuesser(FrotPowerProvider frotPowerProvider, GravPowerProvider gravPowerProvider) {
        this.frotPowerProvider = frotPowerProvider;
        this.gravPowerProvider = gravPowerProvider;
    }

    public void guess(GPXPath path, Cyclist cyclist, WindProvider windProvider) {
        Course course = new Course(path, Instant.now(), cyclist, new PowerProviderFromData(), windProvider, null);
        List<Point> points = path.getPoints();
        CyclistStatus status = new CyclistStatus();
        for (int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);
            Point pp1 = points.get(i + 1);

            double cx = 0.3;
            double speed = p.getSpeed();
            double grade = p.getGrade();
            if (grade > 0.04) {
                cx = 0.3;
            } else if (grade < -0.04) {
                cx = 0.2;
            } else if (speed >= 1.0) {

                // double acc = (power + grav + frot + aero) / (Constants.G * mKg);
                double acc = (pp1.getSpeed() - speed) / (pp1.getEpochSeconds() - p.getEpochSeconds());
                p.putDebug("gcx_acc", acc, Unit.DOUBLE_ANY);
                double p_tot = 0.0; // acc * Constants.G * cyclist.getMKg();
                p.putDebug("gcx_p_tot", p_tot, Unit.WATTS);

                status.setSpeed(speed);
                double frot = frotPowerProvider.getPowerW(course, p, status);
                p.putDebug("gcx_frot", frot, Unit.WATTS);
                double grav = gravPowerProvider.getPowerW(course, p, status);
                p.putDebug("gcx_grav", grav, Unit.WATTS);
                double power = p.getPower();
                p.putDebug("gcx_tot", power, Unit.WATTS);

                // p_tot = frot + grav + aero + power
                double aero = p_tot - frot - grav - power;
                p.putDebug("gcx_aero", aero, Unit.WATTS);
                // aero = -cx * speed * speed * speed;
                cx = -aero / (speed * speed * speed);
                p.putDebug("gcx_cx", cx, Unit.DOUBLE_ANY);
                if (cx < 0.01) {
                    cx = 0.01;
                } else if (cx > 0.5) {
                    cx = 0.5;
                }
            }

            p.put("cx", cx, Unit.CX);
        }
    }

}
