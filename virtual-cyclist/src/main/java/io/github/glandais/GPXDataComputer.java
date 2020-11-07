package io.github.glandais;

import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.MagicPower2MapSpace;
import io.github.glandais.util.Vector;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.cx.CxProviderConstant;
import io.github.glandais.virtual.power.PowerProviderConstant;
import io.github.glandais.virtual.power.PowerProviderConstantWithTiring;
import io.github.glandais.virtual.wind.Wind;
import io.github.glandais.virtual.wind.WindProviderConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
public class GPXDataComputer {

    private final PowerComputer powerComputer;

    private final GPXElevationFixer gpxElevationFixer;

    private final MaxSpeedComputer maxSpeedComputer;

    public GPXDataComputer(final PowerComputer powerComputer,
                           final GPXElevationFixer gpxElevationFixer,
                           final MaxSpeedComputer maxSpeedComputer) {

        this.powerComputer = powerComputer;
        this.gpxElevationFixer = gpxElevationFixer;
        this.maxSpeedComputer = maxSpeedComputer;
    }

    public boolean isCrossing(GPXPath path) {

        // 50m
        GPXFilter.filterPointsDouglasPeucker(path, 50);
        if (path.getPoints()
                .size() > 2) {

            for (int i = 0;
                 i < path.getPoints().size() - 1; i++) {
                for (int j = i + 2;
                     j < path.getPoints().size() - 1; j++) {

                    if (isIntersects(path, i, j)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isIntersects(final GPXPath path, final int i, final int j) {

        final boolean intersects;
        final Point s1p1 = path.getPoints().get(i);
        final Point s1p2 = path.getPoints().get(i + 1);

        final double x1 = s1p1.getLon();
        final double y1 = s1p1.getLat();
        final double x2 = s1p2.getLon();
        final double y2 = s1p2.getLat();

        final Point s2p1 = path.getPoints().get(j);
        final Point s2p2 = path.getPoints().get(j + 1);
        final double x3 = s2p1.getLon();
        final double y3 = s2p1.getLat();
        final double x4 = s2p2.getLon();
        final double y4 = s2p2.getLat();

        final double v = (x4 - x3) * (y1 - y2) - (x1 - x2) * (y4 - y3);
        if (v == 0) {
            intersects = false;
        } else {

            final double ta = ((y3 - y4) * (x1 - x3) + (x4 - x3) * (y1 - y3)) / v;
            final double tb = ((y1 - y2) * (x1 - x3) + (x2 - x1) * (y1 - y3)) / v;

            if ((ta >= 0.0f && ta <= 1.0f) && (tb >= 0.0f && tb <= 1.0f)) {

                intersects = true;
            } else {
                intersects = false;
            }
        }
        return intersects;
    }

    public Vector getWindNew(GPXPath gpxPath, double power) {

        gpxElevationFixer.fixElevation(gpxPath);
        Cyclist cyclist = new Cyclist();
        Wind noWind = new Wind(0, 0);
        Course course = getCourse(gpxPath, power, cyclist, noWind, 0);
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
        long[] time = course.getGpxPath().getTime();
        double duration = (time[time.length - 1] - time[0]) / 1000.0;

        int count = 18;
        long[] dur = new long[count];
        long longMinDur = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {

            int deg = i * (360 / count);

            Wind wind = new Wind(3, Math.toRadians(deg));
            course = getCourse(gpxPath, power, cyclist, wind, duration);
            powerComputer.computeTrack(course);
            time = course.getGpxPath().getTime();
            dur[i] = time[time.length - 1] - time[0];
            longMinDur = Math.min(longMinDur, dur[i]);
        }
        for (int i = 0; i < count; i++) {

            int deg = i * (360 / count);
            final long ms = dur[i] - longMinDur;
            final long s = ms / 1000;
            final long m = ms / 60000;
            System.out.println(deg + "° " + dur[i] + " (" + ms + "ms = " + s + "s = " + m + "m)");
        }
        return getWind(gpxPath);
    }

    private Course getCourse(GPXPath gpxPath, double power, Cyclist cyclist, Wind wind, double duration) {
        PowerProviderConstant powerProvider;
        if (duration == 0) {
            powerProvider = new PowerProviderConstant(power);
        } else {
            powerProvider = new PowerProviderConstantWithTiring(power, duration);
        }
        return new Course(gpxPath, Instant.now(), cyclist, powerProvider, new WindProviderConstant(wind), new CxProviderConstant());
    }

    public Vector getWind(GPXPath path) {

        final List<Point> points = path.getPoints();
        final int size = points.size();
        if (size > 3) {

            final Vector start = project(points.get(0));

            Vector tot = new Vector(0, 0);
            for (Point point : points) {
                tot = tot.add(vector(start, project(point)));
            }
            tot = tot.mul(1.0 / size);
            return tot.normalize().mul(-1.0);
        } else {
            return new Vector(0.0, 0.0);
        }

    }

    private Vector vector(final Vector p1, final Vector p2) {

        return new Vector(p2.getX() - p1.getX(), p2.getY() - p1.getY()).normalize();
    }

    private Vector project(final Point point) {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(point.getLonDeg(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(point.getLatDeg(), 12));
    }

}
