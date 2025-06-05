package io.github.glandais.gpx.virtual.maxspeed;

import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.util.Vector;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.Cyclist;
import jakarta.inject.Singleton;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class MaxSpeedComputer {

    public void computeMaxSpeeds(Course course) {

        // first pass, forward : max speed by incline
        firstPass(course);

        // second pass, reverse : max speed with braking
        secondPass(course);
    }

    protected void firstPass(Course course) {
        List<Point> points = course.getGpxPath().getPoints();
        Cyclist cyclist = course.getCyclist();
        for (int i = 0; i < points.size(); i++) {
            Point p = points.get(i);
            if (i == 0) {
                // no info for first point
                p.setSpeedMax(cyclist.getMaxSpeedMs());
            } else if (i == points.size() - 1) {
                // stop at last point
                p.setSpeedMax(2);
            } else {
                // point before/point after
                Point pm1 = points.get(i - 1);
                Point pp1 = points.get(i + 1);
                // compute max speed
                computeMaxSpeedByIncline(pm1, p, pp1, cyclist);
            }
            p.putDebug(PropertyKeys.speed_max_incline, p.getSpeedMax());
        }
    }

    protected void secondPass(Course course) {
        List<Point> points = course.getGpxPath().getPoints();
        Cyclist cyclist = course.getCyclist();
        for (int i = points.size() - 1; i > 0; i--) {
            Point p = points.get(i);
            Point pm1 = points.get(i - 1);
            computeMaxSpeedByBraking(pm1, p, cyclist);
        }
    }

    private void computeMaxSpeedByIncline(Point pm1, Point p, Point pp1, Cyclist cyclist) {

        // relative position of meters, in meters
        Vector tpm1 = transform(pm1, p);
        Vector tp = new Vector(0, 0, 0);
        Vector tpp1 = transform(pp1, p);

        // find center of circle going through the 3 points
        Vector circleCenter = getCircleCenter(tpm1, tp, tpp1);
        if (circleCenter == null) {
            // not found, either 3 points are equal or colinear
            p.setSpeedMax(cyclist.getMaxSpeedMs());
            return;
        }
        Vector rad = circleCenter.sub(tp);
        // circle radius (m)
        double radius = Math.hypot(rad.x(), rad.y());
        // add 2m for trajectory, a trajectory computer would be better
        radius = radius + 2;
        p.putDebug(PropertyKeys.radius, radius);

        // https://en.wikipedia.org/wiki/Bicycle_and_motorcycle_dynamics#Leaning
        double vmax = Math.sqrt(Constants.G * radius * cyclist.getTanMaxAngle());
        p.setSpeedMax(Math.min(cyclist.getMaxSpeedMs(), vmax));
    }

    private void computeMaxSpeedByBraking(Point pm1, Point p, Cyclist cyclist) {
        double v0 = pm1.getSpeedMax();
        double vf = p.getSpeedMax();
        double a = -cyclist.getMaxBrakeMS2();

        double t = (vf - v0) / a;
        if (t <= 0) {
            // no need to brake
            return;
        }
        double dist = p.getDist() - pm1.getDist();

        double dBrake = v0 * t + (a * t * t) / 2;
        if (dBrake <= dist) {
            // no need to reduce v0
            // enough braking available on dist to go from v0 to vf
            return;
        }
        double newMaxSpeedPrevious = Math.sqrt(vf * vf - 2 * a * dist);
        pm1.setSpeedMax(newMaxSpeedPrevious);
    }

    private static Vector getCircleCenter(Vector a, Vector b, Vector c) {
        double ax = a.x();
        double ay = a.y();
        double bx = b.x();
        double by = b.y();
        double cx = c.x();
        double cy = c.y();

        double A = bx - ax;
        double B = by - ay;
        double C = cx - ax;
        double D = cy - ay;

        double E = A * (ax + bx) + B * (ay + by);
        double F = C * (ax + cx) + D * (ay + cy);

        double G = 2 * (A * (cy - by) - B * (cx - bx));
        if (Math.abs(G) < 0.001) return null; // a, b, c must be collinear

        double px = (D * E - B * F) / G;
        double py = (A * F - C * E) / G;
        return new Vector(px, py, 0);
    }

    private Vector transform(Point point, Point pRef) {
        double lon = (point.getLon() - pRef.getLon());
        double lat = (point.getLat() - pRef.getLat());
        double x = (lon / (2 * Math.PI)) * Constants.CIRC * Math.cos(pRef.getLat());
        double y = (lat / (2 * Math.PI)) * Constants.CIRC;
        return new Vector(x, y, 0);
    }
}
