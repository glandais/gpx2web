package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.gpx.util.SmoothService;
import io.github.glandais.gpx.virtual.power.PowerComputer;
import io.github.glandais.gpx.virtual.power.cyclist.GradeSpeedService;
import io.github.glandais.gpx.virtual.power.cyclist.GradeSpeeds;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static io.github.glandais.gpx.virtual.Constants.DT;

@RequiredArgsConstructor
@Service
@Singleton
@Slf4j
public class VirtualizeService {

    private final PowerComputer powerComputer;

    private final GradeSpeedService gradeSpeedService;

    private final SmoothService smoothService;

    public void virtualizeTrack(Course course) {

        GradeSpeeds gradeSpeeds = new GradeSpeeds(gradeSpeedService, course);
        course.setGradeSpeeds(gradeSpeeds);

        final List<Point> newPoints = new ArrayList<>();

        Instant start = course.getStart();
        Instant now = start;

        GPXPath gpxPath = course.getGpxPath();
        List<Point> input = gpxPath.getPoints();

        // current is first point
        Point current = input.get(0).copy();
        current.setDist(0, ValueKind.staging);
        current.setInstant(now, ValueKind.staging);
        current.computeElapsedTime(now, ValueKind.staging);
        current.setSpeed(Constants.MINIMAL_SPEED, ValueKind.staging);
        newPoints.add(current);

        while (current.getDist() != gpxPath.getDist()) {
            int index = getNextIndex(input, current.getDist(), 0.0);

            double currentSpeed = current.getSpeed();
            double speedNew = powerComputer.getNewSpeedAfterDt(course, current);
            double dx = DT * (currentSpeed + speedNew) / 2;

            int newIndex = getNextIndex(input, current.getDist(), dx);
            double dxToNext;
            double dtToNext;
            if (index != newIndex) {
                dxToNext = input.get(newIndex).getDist() - current.getDist();
                dtToNext = DT * dxToNext / dx;

                current = input.get(newIndex).copy();
            } else {
                dxToNext = dx;
                dtToNext = DT;

                double newDist = current.getDist() + dx;
                double p1dist = input.get(index).getDist();
                double p2dist = input.get(index + 1).getDist();
                double coef = (newDist - p1dist) / (p2dist - p1dist);
                current = Point.interpolate(input.get(index), input.get(index + 1), coef);
            }

            speedNew = 2 * (dxToNext / dtToNext) - currentSpeed;
            if (speedNew > current.getSpeedMax()) {
                speedNew = current.getSpeedMax();
            }
            dtToNext = 2 * dxToNext / (currentSpeed + speedNew);

            current.setSpeed(speedNew, ValueKind.staging);

            Duration dtToNextDuration = getDuration(dtToNext);
            now = now.plus(dtToNextDuration);
            current.setInstant(now, ValueKind.staging);
            current.computeElapsedTime(start, ValueKind.staging);
            newPoints.add(current);
        }

        final List<Point> realNewPoints = new ArrayList<>();
        for (int i = 0; i < newPoints.size(); i++) {
            if (i == newPoints.size() - 1) {
                realNewPoints.add(newPoints.get(i));
            } else {
                double dx = newPoints.get(i + 1).getDist() - newPoints.get(i).getDist();
                if (dx >= 1.0) {
                    realNewPoints.add(newPoints.get(i));
                }
            }
        }
        for (int i = 0; i < realNewPoints.size() - 1; i++) {
            double cyclistPower = powerComputer.computeCyclistPower(course, realNewPoints.get(i), realNewPoints.get(i + 1));
            realNewPoints.get(i).putDebug("p_cyclist_recomputed", cyclistPower, Unit.WATTS);
            realNewPoints.get(i).setPower(cyclistPower, ValueKind.staging);
        }
        gpxPath.setPoints(realNewPoints, ValueKind.computed);
        smoothService.smoothPower(gpxPath);
    }

    private Duration getDuration(double dtToNext) {
        long fullSeconds = (long) dtToNext;
        long nanoAdjustment = (long) ((dtToNext - fullSeconds) * 1_000_000_000);
        return Duration.ofSeconds(fullSeconds, nanoAdjustment);
    }

    private int getNextIndex(List<Point> input, double dist, double dx) {
        int i1 = getIndex(input, dist);
        int i2 = getIndex(input, dist + dx);
        if (i1 != i2) {
            return i1 + 1;
        }
        return i1;
    }

    private int getIndex(List<Point> points, double dist) {
        for (int i = points.size() - 1; i >= 0; i--) {
            if (dist >= points.get(i).getDist()) {
                return i;
            }
        }
        return -1;
    }

}
