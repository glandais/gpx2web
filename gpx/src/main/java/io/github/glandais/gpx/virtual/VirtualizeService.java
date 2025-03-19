package io.github.glandais.gpx.virtual;

import static io.github.glandais.gpx.virtual.Constants.DT;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.converter.Converters;
import io.github.glandais.gpx.util.SmoothService;
import io.github.glandais.gpx.virtual.power.PowerComputer;
import io.github.glandais.gpx.virtual.power.cyclist.GradeSpeedService;
import io.github.glandais.gpx.virtual.power.cyclist.GradeSpeeds;
import jakarta.inject.Singleton;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        double equivalentMass = powerComputer.getEquivalentMass(course);

        final List<Point> newPoints = new ArrayList<>();

        Instant start = course.getStart();
        Instant now = start;

        GPXPath gpxPath = course.getGpxPath();
        double[] dists = gpxPath.getDists();
        int distsLength = dists.length;
        List<Point> input = gpxPath.getPoints();

        // current is first point
        Point current = input.get(0).copy();
        current.setDist(0);
        current.setInstant(start, now);
        current.setSpeed(Constants.MINIMAL_SPEED);
        newPoints.add(current);

        while (current.getDist() != gpxPath.getDist()) {
            int index = getNextIndex(dists, distsLength, current.getDist(), 0.0);

            double currentSpeed = current.getSpeed();
            current.putDebug(PropertyKeys.virt_speed_current, currentSpeed);
            double pSum = powerComputer.getNewPower(course, current, true);
            double dx = powerComputer.getDx(pSum, equivalentMass, currentSpeed, DT);

            int newIndex = getNextIndex(dists, distsLength, current.getDist(), dx);
            double dxToNext;
            double dtToNext;
            if (index != newIndex) {
                dxToNext = input.get(newIndex).getDist() - current.getDist();
                dtToNext = powerComputer.getDt(pSum, equivalentMass, currentSpeed, dxToNext);
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

            double speedNew = 2 * (dxToNext / dtToNext) - currentSpeed;
            if (speedNew > current.getSpeedMax()) {
                speedNew = current.getSpeedMax();
            }
            dtToNext = 2 * dxToNext / (currentSpeed + speedNew);

            current.setSpeed(speedNew);

            Duration dtToNextDuration = getDuration(dtToNext);
            now = now.plus(dtToNextDuration);
            current.setInstant(start, now);
            newPoints.add(current);
        }

        for (int i = 0; i < newPoints.size() - 1; i++) {
            double cyclistPower =
                    powerComputer.computeCyclistPower(course, equivalentMass, newPoints.get(i), newPoints.get(i + 1));
            newPoints.get(i).setPower(cyclistPower);
        }
        gpxPath.setPoints(newPoints);
        //        smoothService.smoothPower(gpxPath);
    }

    private Duration getDuration(double dtToNext) {
        return Converters.DURATION_SECONDS_CONVERTER.convertToStorage(dtToNext);
    }

    private int getNextIndex(double[] dists, int distsLength, double dist, double dx) {
        int i1 = getIndex(dists, distsLength, dist);
        int i2 = getIndex(dists, distsLength, dist + dx);
        if (i1 != i2) {
            return i1 + 1;
        }
        return i1;
    }

    private int getIndex(double[] dists, int distsLength, double dist) {
        int left = 0;
        int right = distsLength - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;

            if (dists[mid] <= dist && (mid == distsLength - 1 || dist < dists[mid + 1])) {
                return mid;
            }

            if (dists[mid] < dist) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }
}
