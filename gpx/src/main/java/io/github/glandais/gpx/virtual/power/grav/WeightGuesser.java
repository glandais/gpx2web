package io.github.glandais.gpx.virtual.power.grav;

import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Singleton
public class WeightGuesser {
/*
    private final PowerProviderList providers;

    public void guess(GPXPath path, Cyclist cyclist, WindProvider windProvider, AeroProvider aeroProvider) {
        Course course = new Course(path, Instant.now(), cyclist, new PowerProviderFromData(), windProvider, aeroProvider);
        List<Point> points = path.getPoints();
        CyclistStatus status = new CyclistStatus();
        for (int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);

            double mKg = -1.0;
            double speed = p.getSpeed();
            double grade = p.getGrade();
            if (speed >= 1.0 && grade > 0.04) {

                status.setSpeed(speed);

                double power = p.getPower();
                // no acceleration, no energy left
                // 0 = grav + (sum other powers) + power
                double p_grav = -power;
                for (PowerProvider powerProvider : providers.getPowerProviders()) {
                    String id = powerProvider.getId();
                    if (!id.equals("gravity")) {
                        double powerW = powerProvider.getPowerW(course, p, status);
                        p.putDebug("gw_p_" + id, powerW, Unit.WATTS);
                        p_grav = p_grav - powerW;
                    }
                }
                p.putDebug("gw_p_grav", p_grav, Unit.WATTS);

                double coef = Math.sin(Math.atan(grade));
                //double p_grav = -mKg * Constants.G * status.getSpeed() * coef;
                mKg = -p_grav / (Constants.G * status.getSpeed() * coef);
                p.putDebug("gw_mKg", mKg, Unit.DOUBLE_ANY);
                if (mKg < 40) {
                    mKg = -1.0;
                } else if (mKg > 130.0) {
                    mKg = -1.0;
                }
            }

            if (mKg > 0) {
                p.put(PointField.mKg, mKg, Unit.DOUBLE_ANY, ValueKind.guessed);
            }
        }
    }
*/
}
