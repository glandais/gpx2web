package io.github.glandais.gpx.virtual.power.aero.aero;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
public class AeroGuesser {
/*
    private final PowerProviderList providers;

    public void guess(GPXPath path, Cyclist cyclist, WindProvider windProvider) {
        Course course = new Course(path, Instant.now(), cyclist, new PowerProviderFromData(), windProvider, null);
        List<Point> points = path.getPoints();
        CyclistStatus status = new CyclistStatus();
        for (int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);

            double cx = 0.3;
            double speed = p.getSpeed();
            double grade = p.getGrade();
            if (grade > 0.04) {
                cx = 0.3;
            } else if (grade < -0.04) {
                cx = 0.2;
            } else if (speed >= 1.0) {

                status.setSpeed(speed);
                double power = p.getPower();
                p.putDebug("gcx_p_tot", power, Unit.WATTS);
                // no acceleration, no energy left
                // 0 = aero + (sum other powers) + power
                double aero = -power;
                for (PowerProvider powerProvider : providers.getPowerProviders()) {
                    String id = powerProvider.getId();
                    if (!id.equals("aero")) {
                        double powerW = powerProvider.getPowerW(course, p, status);
                        p.putDebug("gcx_p_" + id, powerW, Unit.WATTS);
                        aero = aero - powerW;
                    }
                }
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

            p.put(PointField.aeroCoef, cx, Unit.AERO_COEF, ValueKind.guessed);
        }
    }
*/
}
