package io.github.glandais;

import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.map.MagicPower2MapSpace;
import io.github.glandais.map.Vector;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
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
                 i < path.getPoints()
                         .size() - 1;
                 i++) {
                for (int j = i + 2;
                     j < path.getPoints()
                             .size() - 1;
                     j++) {

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
        final Point s1p1 = path.getPoints()
                .get(i);
        final Point s1p2 = path.getPoints()
                .get(i + 1);

        final double x1 = s1p1.getLon();
        final double y1 = s1p1.getLat();
        final double x2 = s1p2.getLon();
        final double y2 = s1p2.getLat();

        final Point s2p1 = path.getPoints()
                .get(j);
        final Point s2p2 = path.getPoints()
                .get(j + 1);
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

    public Vector getWindNew(GPXPath gpxPath) {

        gpxElevationFixer.fixElevation(gpxPath);
        double mKg = 72;
        double powerW = 280;
        double maxAngleDeg = 15;
        double maxSpeedKmH = 90;
        double maxBrakeG = 0.3;
        Cyclist cyclist = new Cyclist(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG);
        Course course = new Course(gpxPath, cyclist, ZonedDateTime.now(), 0, 0);
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
        long[] time = course.getGpxPath().getTime();
        long duration = time[time.length - 1] - time[0];

        int count = 18;
        long[] dur = new long[count];
        long longMinDur = Long.MAX_VALUE;
        for (int i = 0; i < count; i++) {

            int deg = i * (360 / count);
            course = new Course(gpxPath, cyclist, ZonedDateTime.now(), 3, Math.toRadians(deg));
            powerComputer.computeTrack(course);
            time = course.getGpxPath().getTime();
            dur[i] = time[time.length - 1] - time[0];
            longMinDur = Math.min(longMinDur, dur[i]);
        }
        for (int i = 0; i < count; i++) {

            int deg = i * (360 / count);
            System.out.println(deg + "° " + dur[i] + " (" + (dur[i] - longMinDur) + ")");
        }
        return getWind(gpxPath);
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
            return tot.normalize()
                    .mul(-1.0);
        } else {
            return new Vector(0.0, 0.0);
        }

    }

    private Vector vector(final Vector p1, final Vector p2) {

        return new Vector(p2.getX() - p1.getX(), p2.getY() - p1.getY()).normalize();
    }

    private Vector project(final Point point) {

        return new Vector(MagicPower2MapSpace.INSTANCE_256.cLonToX(point.getLon(), 12),
                MagicPower2MapSpace.INSTANCE_256.cLatToY(point.getLat(), 12));
    }

}
