package io.github.glandais.virtual.cyclist;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.CyclistStatus;
import io.github.glandais.virtual.cx.AeroPowerProvider;
import io.github.glandais.virtual.cx.CxProvider;
import io.github.glandais.virtual.frot.FrotPowerProvider;
import io.github.glandais.virtual.wind.WindProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class WeightGuesser {

    private final FrotPowerProvider frotPowerProvider;

    private final AeroPowerProvider aeroPowerProvider;

    public WeightGuesser(FrotPowerProvider frotPowerProvider, AeroPowerProvider aeroPowerProvider) {
        this.frotPowerProvider = frotPowerProvider;
        this.aeroPowerProvider = aeroPowerProvider;
    }

    public void guess(GPXPath path, Cyclist cyclist, WindProvider windProvider, CxProvider cxProvider) {
        Course course = new Course(path, Instant.now(), cyclist, new PowerProviderFromData(), windProvider, cxProvider);
        List<Point> points = path.getPoints();
        CyclistStatus status = new CyclistStatus();
        for (int i = 0; i < points.size() - 1; i++) {
            Point p = points.get(i);
            Point pp1 = points.get(i + 1);

            double mKg = -1.0;
            double speed = p.getSpeed();
            double grad = p.getGrade();
            if (speed >= 1.0 && grad > 0.04) {

                // double acc = (power + grav + frot + aero) / (Constants.G * mKg);
                double acc = (pp1.getSpeed() - speed) / (pp1.getEpochSeconds() - p.getEpochSeconds());
                p.putDebug("gw_acc", acc, Unit.DOUBLE_ANY);
                double p_tot = 0.0;//acc * Constants.G * cyclist.getMKg();
                p.putDebug("gw_p_tot", p_tot, Unit.WATTS);

                status.setSpeed(speed);
                double frot = frotPowerProvider.getPowerW(course, p, status);
                p.putDebug("gw_frot", frot, Unit.WATTS);
                double aero = aeroPowerProvider.getPowerW(course, p, status);
                p.putDebug("gw_aero", aero, Unit.WATTS);
                double power = p.getPower();
                p.putDebug("gw_tot", power, Unit.WATTS);

                // p_tot = frot + grav + aero + power
                double p_grav = p_tot - frot - aero - power;
                p.putDebug("gw_grav", power, Unit.WATTS);
                p.putDebug("gw_grad", grad, Unit.PERCENTAGE);
                // p_grav = -mKg * Constants.G * status.getSpeed() * grad;
                mKg = -p_grav / (Constants.G * status.getSpeed() * grad);
                p.putDebug("gw_mKg", mKg, Unit.DOUBLE_ANY);
                if (mKg < 40) {
                    mKg = -1.0;
                } else if (mKg > 130.0) {
                    mKg = -1.0;
                }
            }

            if (mKg > 0) {
                p.put("mKg", mKg, Unit.DOUBLE_ANY);
            }
        }
    }

}
