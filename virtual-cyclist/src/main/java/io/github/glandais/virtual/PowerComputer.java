package io.github.glandais.virtual;

import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.Point;
import io.github.glandais.util.Constants;
import io.github.glandais.util.MagicPower2MapSpace;
import io.github.glandais.util.Vector;
import io.github.glandais.virtual.wind.Wind;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PowerComputer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputer.class);

    // m.s-2, minimal speed = 2km/h
    private static final double MINIMAL_SPEED = 2.0 / 3.6;
    // s
    private static final double DT = 0.25;

    public void computeTrack(Course course) {

        final List<Point> newPoints = new ArrayList<>();

        final CyclistStatus status = new CyclistStatus();

        while (status.odo < course.getGpxPath().getDist()) {
            newPoints.add(getNextPoint(course, status));
        }

        course.getGpxPath().setPoints(newPoints);
        course.getGpxPath().computeArrays();
    }

    private Point getNextPoint(Course course, CyclistStatus status) {
        final long startMillis = course.getStart().toEpochMilli();
        long now = startMillis + ((long) (1000 * status.ellapsed));
        Point current = getPoint(course, status.odo, now);
        current.setTime(Instant.ofEpochMilli(now));

        current.getData().put("0_0_odo", status.odo);
        current.getData().put("0_1_speed", status.speed);

        double p_grav = computePGrav(course, status, current);
        double p_frot = computePFrot(course, status, current);
        double p_air = computePAir(course, status, current);
        double p_cyclist = getPCyclist(course, status, current, p_grav, p_frot, p_air);

        // p_app = cyclist power - resistance
        double p_app = p_cyclist - p_air - p_frot - p_grav;
        current.getData().put("5_1_p_app", p_app);

        final double mKg = course.getCyclist().getMKg();
        // m.s-2
        double acc = p_app / mKg;
        current.getData().put("5_2_acc", acc);

        status.speed = status.speed + acc * DT;
        current.getData().put("5_3_new_speed", status.speed);

        status.speed = Math.max(MINIMAL_SPEED, Math.min(current.getMaxSpeed(), status.speed));
        current.getData().put("5_4_fixed_speed", status.speed);

        current.setSpeed(status.speed);

        double dx = DT * status.speed;
        current.getData().put("5_5_dx", dx);

        status.odo = status.odo + dx;
        status.ellapsed = status.ellapsed + DT;
        //log.info("{}s {}m", ellapsed, odo);
        return current;
    }

    private double getPCyclist(Course course, CyclistStatus status, Point current, double p_grav, double p_frot, double p_air) {
        double grad = current.getData().get("grad");
        double p_cyclist = course.getPowerProvider().getPowerW(current, status.ellapsed, p_air, p_frot, p_grav, status.speed, grad);
        current.getData().put("5_0_p_cyclist", p_cyclist);
        return p_cyclist;
    }

    private double computePGrav(Course course, CyclistStatus status, Point current) {
        final double mKg = course.getCyclist().getMKg();
        double grad = current.getData().get("grad");
        double p_grav = mKg * Constants.G * status.speed * grad;
        current.getData().put("2_2_p_grav", p_grav);
        return p_grav;
    }

    private double computePFrot(Course course, CyclistStatus status, Point current) {
        final double mKg = course.getCyclist().getMKg();
        final double f = course.getCyclist().getF();
        double p_frot = mKg * Constants.G * status.speed * f;
        current.getData().put("3_0_f", f);
        current.getData().put("3_1_p_frot", p_frot);
        return p_frot;
    }

    private double computePAir(Course course, CyclistStatus status, Point current) {
        double grad = current.getData().get("grad");
        final double cx = course.getCxProvider().getCx(current, status.ellapsed, status.speed, grad);
        final Wind wind = course.getWindProvider().getWind(current, status.ellapsed);
        current.getData().put("4_0_cx", cx);
        current.getData().put("4_1_wind_speed", wind.getWindSpeed());
        current.getData().put("4_2_wind_direction", wind.getWindDirection());
        double p_air;
        if (wind.getWindSpeed() == 0) {
            p_air = cx * status.speed * status.speed * status.speed;
        } else {
            p_air = computePAirWithWind(status, current, cx, wind);
        }
        current.getData().put("4_6_p_air", p_air);
        return p_air;
    }

    private double computePAirWithWind(CyclistStatus status, Point current, double cx, Wind wind) {
        double bearing = current.getData().get("bearing");
        current.getData().put("4_3_cyclist_bearing", bearing);
        double windDirectionAsBearing = (Math.PI / 2) - wind.getWindDirection();
        current.getData().put("4_4_wind_bearing", windDirectionAsBearing);

        double alpha = windDirectionAsBearing - bearing;
        current.getData().put("4_5_wind_alpha", alpha);

        double v = wind.getWindSpeed();

        // https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf

        double l1 = status.speed + v * Math.cos(alpha);
        double l2 = Math.pow(l1, 2);
        double l3 = status.speed * status.speed + v * v + 2 * status.speed * v * Math.cos(alpha);
        double l4 = l2 / l3;

        double mu = 1.2;
        double lambda = l4 + mu * (1 - l4);

        return cx * lambda * Math.sqrt(l3) * l1 * status.speed;
    }

    private Point getPoint(Course course, double d, long now) {

        List<Point> points = course.getGpxPath().getPoints();
        double[] dists = course.getGpxPath().getDists();
        for (int i = 0; i < points.size(); i++) {
            if (d <= dists[i]) {
                if (i == 0) {
                    points.get(0).getData().put("i", 0.0);
                    points.get(0).getData().put("grad", 0.0);
                    points.get(0).getData().put("bearing", 0.0);
                    return points.get(0);
                } else {
                    Point p = points.get(i - 1);
                    Point pp1 = points.get(i);
                    if (dists[i] - dists[i - 1] == 0) {
                        continue;
                    }

                    double dz = pp1.getZ() - p.getZ();
                    double grad = dz / (dists[i] - dists[i - 1]);

                    Vector v_from = project(p);
                    Vector v_to = project(pp1);
                    double dy2 = v_to.getY() - v_from.getY();
                    double dx2 = v_to.getX() - v_from.getX();
                    double bearing = Math.atan2(-dy2, dx2);

                    double ratio = (d - dists[i - 1]) / (dists[i] - dists[i - 1]);
                    Point result = GPXPerSecond.getPoint(p, pp1, ratio, now);
                    result.getData().put("i", (double) (i - 1));
                    result.getData().put("grad", grad);
                    result.getData().put("bearing", bearing);
                    return result;
                }
            }
        }
        throw new IllegalStateException();
    }

    private Vector project(final Point point) {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(point.getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(point.getLatDeg(), 12));
    }

}
