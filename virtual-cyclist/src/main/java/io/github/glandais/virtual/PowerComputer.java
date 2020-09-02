package io.github.glandais.virtual;

import io.github.glandais.gpx.Point;
import io.github.glandais.map.MagicPower2MapSpace;
import io.github.glandais.map.Vector;
import io.github.glandais.util.Constants;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PowerComputer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputer.class);

    // m/h, initial speed = 5km/h
    private static final double INITIAL_SPEED = 2.0 / 3.6;

    public void computeTrack(Course course) {

        List<Point> points = course.getGpxPath()
                .getPoints();

        final long startTime = course.getStart()
                .toInstant()
                .toEpochMilli();
        long currentTime = startTime;

        double u = INITIAL_SPEED;
        double odo = 0.0;
        for (int j = 0; j < points.size(); j++) {
            Point to = points.get(j);
            double dist = 0;
            if (j > 0) {
                Point from = points.get(j - 1);
                from.getData()
                        .put("v", 3.6 * u);
                // meters
                dist = 1000.0 * from.distanceTo(to);
                if (dist > 0) {
                    // point to point result
                    PointToPoint result = computePointToPoint(u, odo, from, to, currentTime - startTime, course);

                    u = result.getEndSpeed();

                    // ms ellapsed time
                    long ts = Math.round(result.getTime() * 1000);
                    currentTime = currentTime + ts;
                }
            }
            odo = odo + dist;
            to.setTime(currentTime);
        }
        course.getGpxPath()
                .computeArrays();
    }

    private PointToPoint computePointToPoint(double u, double odo, Point from, Point to, long currentTime, Course course) {

        double dist = 1000.0 * from.distanceTo(to);

        double dz = to.getZ() - from.getZ();
        double grad = dz / dist;

        LOGGER.debug("{} {} {} {} {}", odo, u, from, to, grad);

        // max speeds
        double ms1 = from.getMaxSpeed();
        double ms2 = to.getMaxSpeed();

        // we will split the distance
        // m
        double d = 0.0;
        // s
        double t = 0.0;
        // s
        double dt = 0.25;

        double mKg = course.getCyclist()
                .getMKg();
        double cx = course.getCyclist()
                .getCx();
        double f = course.getCyclist()
                .getF();

        // OK
        // (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
        double p_grav, p_air, p_frot, p_cyclist, p_app, acc, c, ms, dx;
        double prev_v, prev_d, prev_t;
        while (true) {
            prev_v = u;
            prev_d = d;
            prev_t = t;
            // OK
            // (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
            p_grav = mKg * Constants.G * u * grad;
            p_frot = f * Constants.G * mKg * u;

            if (course.getWindSpeed() == 0) {
                p_air = cx * u * u * u;
            } else {

                Vector v_from = project(from);
                Vector v_to = project(to);
                double dy2 = v_to.getY() - v_from.getY();
                double dx2 = v_to.getX() - v_from.getX();
                double bearing = Math.atan2(-dy2, dx2);
                double windDirectionAsBearing = (Math.PI / 2) - course.getWindDirection();

                double alpha = windDirectionAsBearing - bearing;
                double v = course.getWindSpeed();

                // https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf

                double l1 = u + v * Math.cos(alpha);
                double l2 = Math.pow(l1, 2);
                double l3 = u * u + v * v + 2 * u * v * Math.cos(alpha);
                double l4 = l2 / l3;

                double mu = 1.2;
                double lambda = l4 + mu * (1 - l4);

                p_air = cx * lambda * Math.sqrt(l3) * l1 * u;
            }

            p_cyclist = course.getPowerW(from, to, currentTime, p_air, p_frot, p_grav, u, grad);
            from.getData()
                    .put("p", p_cyclist);

            // p_app = cyclist power - resistance
            p_app = p_cyclist - p_air - p_frot - p_grav;

            // m.s-2
            acc = p_app / mKg;
            u = u + acc * dt;

            // Compute max speed
            if (d > dist) {
                ms = ms2;
            } else {
                c = d / dist;
                ms = ms1 + c * (ms2 - ms1);
            }
            if (u > ms) {
                u = ms;
            }
            if (u < INITIAL_SPEED) {
                u = INITIAL_SPEED;
            }

            dx = dt * u;
            d = d + dx;
            t = t + dt;
            if (d > dist) {
                double ratio = (dist - prev_d) / dx;
                double lastTime = prev_t + dt * ratio;
                double lastSpeed = prev_v + acc * dt * ratio;
                if (lastSpeed < INITIAL_SPEED) {
                    lastSpeed = INITIAL_SPEED;
                }
                return new PointToPoint(lastTime, lastSpeed);
            }
        }
    }

    private Vector project(final Point point) {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(point.getLon(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(point.getLat(), 12));
    }

}
