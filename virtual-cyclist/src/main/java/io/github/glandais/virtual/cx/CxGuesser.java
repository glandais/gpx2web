package io.github.glandais.virtual.cx;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import io.github.glandais.virtual.power.FrotPowerProvider;
import io.github.glandais.virtual.power.GravPowerProvider;
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
            if (speed >= 1.0) {

                // double acc = (power + grav + frot + aero) / (Constants.G * mKg);
                double acc = (pp1.getSpeed() - speed) / (pp1.getEpochSeconds() - p.getEpochSeconds());
                double p_tot = acc * Constants.G * cyclist.getMKg();

                status.setSpeed(speed);
                double frot = frotPowerProvider.getPowerW(course, p, status);
                double grav = gravPowerProvider.getPowerW(course, p, status);
                double power = p.getPower();

                // p_tot = frot + grav + aero + power
                double aero = p_tot - frot - grav - power;
                // aero = -cx * speed * speed * speed;
                cx = -aero / (speed * speed * speed);
                if (cx < 0.01) {
                    cx = 0.01;
                } else if (cx > 0.5) {
                    cx = 0.5;
                }
            }

            p.put("cx", cx, Unit.CX);
        }
        // points.get(points.size() - 1).put("cx", points.get(points.size() - 1));
    }

}
