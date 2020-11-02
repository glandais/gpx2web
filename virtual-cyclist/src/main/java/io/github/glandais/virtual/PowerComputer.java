package io.github.glandais.virtual;

import io.github.glandais.gpx.Point;
import io.github.glandais.util.Constants;
import io.github.glandais.util.MagicPower2MapSpace;
import io.github.glandais.util.Vector;
import io.github.glandais.virtual.wind.Wind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PowerComputer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PowerComputer.class);

    // m.s-2, initial speed = 2km/h
    private static final double INITIAL_SPEED = 2.0 / 3.6;

    public void computeTrack(Course course) {

        List<Point> points = course.getGpxPath().getPoints();
        List<Point> newPoints = new ArrayList<>(points.size());

        ZonedDateTime start = course.getStart();

        Point prev = null;

        for (int i = 0; i < points.size(); i++) {
            Point current = points.get(i);
            if (prev != null) {
                // meters
                double dist = prev.distanceTo(current);
                if (dist > 0) {
                    // point to point result
                    computePointToPoint(prev, current, start, course);
                    newPoints.add(current);
                    prev = current;
                }
            } else {
                current.setSpeed(INITIAL_SPEED);
                current.setTime(start);
                newPoints.add(current);
                prev = current;
            }
        }
        course.getGpxPath().setPoints(newPoints);
        course.getGpxPath().computeArrays();
    }

    private void computePointToPoint(Point from, Point to, ZonedDateTime start, Course course) {

        double dist = from.distanceTo(to);

        double dz = to.getZ() - from.getZ();
        double grad = dz / dist;
        double speed = from.getSpeed();

        // max speeds, m.s-2
        double ms1 = from.getMaxSpeed();
        double ms2 = to.getMaxSpeed();

        // we will split the distance
        // m
        double d = 0.0;
        // s
        double t = 0.0;
        // s
        double dt = 0.25;

        double mKg = course.getCyclist().getMKg();
        double f = course.getCyclist().getF();

        // OK
        // (http://fr.wikipedia.org/wiki/Puissance_musculaire_humaine_et_bicyclette)
        double p_grav, p_air, p_frot, p_cyclist, p_app, acc, c, ms, dx;
        double prev_speed, prev_d, prev_t;
        ZonedDateTime now;
        while (true) {
            prev_speed = speed;
            prev_d = d;
            prev_t = t;

            p_grav = mKg * Constants.G * speed * grad;
            p_frot = f * Constants.G * mKg * speed;

            now = from.getTime().plus(Duration.ofNanos((long) (t * 1000 * 1000 * 1000)));
            Duration ellapsed = Duration.between(start, now);
            double cx = course.getCxProvider().getCx(from, to, now, ellapsed, p_frot, p_grav, speed, grad);

            Wind wind = course.getWindProvider().getWind(from, now, ellapsed);
            if (wind.getWindSpeed() == 0) {
                p_air = cx * speed * speed * speed;
            } else {

                Vector v_from = project(from);
                Vector v_to = project(to);
                double dy2 = v_to.getY() - v_from.getY();
                double dx2 = v_to.getX() - v_from.getX();
                double bearing = Math.atan2(-dy2, dx2);
                double windDirectionAsBearing = (Math.PI / 2) - wind.getWindDirection();

                double alpha = windDirectionAsBearing - bearing;
                double v = wind.getWindSpeed();

                // https://www.sheldonbrown.com/isvan/Power%20Management%20for%20Lightweight%20Vehicles.pdf

                double l1 = speed + v * Math.cos(alpha);
                double l2 = Math.pow(l1, 2);
                double l3 = speed * speed + v * v + 2 * speed * v * Math.cos(alpha);
                double l4 = l2 / l3;

                double mu = 1.2;
                double lambda = l4 + mu * (1 - l4);

                p_air = cx * lambda * Math.sqrt(l3) * l1 * speed;
            }

            p_cyclist = course.getPowerProvider().getPowerW(from, to, now, ellapsed, p_air, p_frot, p_grav, speed, grad);

            // p_app = cyclist power - resistance
            p_app = p_cyclist - p_air - p_frot - p_grav;

            // m.s-2
            acc = p_app / mKg;
            speed = speed + acc * dt;

            // Compute max speed
            if (d > dist) {
                ms = ms2;
            } else {
                c = d / dist;
                ms = ms1 + c * (ms2 - ms1);
            }
            if (speed > ms) {
                speed = ms;
            }
            if (speed < INITIAL_SPEED) {
                speed = INITIAL_SPEED;
            }

            dx = dt * speed;
            d = d + dx;
            t = t + dt;
            if (d > dist) {
                double ratio = (dist - prev_d) / dx;
                double lastTime = prev_t + dt * ratio;
                double lastSpeed = prev_speed + acc * dt * ratio;
                if (lastSpeed < INITIAL_SPEED) {
                    lastSpeed = INITIAL_SPEED;
                }

                now = from.getTime().plus(Duration.ofNanos((long) (lastTime * 1000 * 1000 * 1000)));
                to.setTime(now);
                to.getData().put("speed", lastSpeed);
                to.getData().put("power", p_cyclist);
                to.getData().put("p_air", p_air);
                to.getData().put("p_frot", p_frot);
                to.getData().put("p_grav", p_grav);
                return;
            }
        }
    }

    private Vector project(final Point point) {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(point.getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(point.getLatDeg(), 12));
    }

}
