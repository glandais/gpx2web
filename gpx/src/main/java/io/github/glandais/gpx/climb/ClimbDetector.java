package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.util.Simplifier;
import io.github.glandais.gpx.util.Vector;
import jakarta.inject.Singleton;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Singleton
public class ClimbDetector {

    private final Simplifier<ClimbPoint> simplifier = new Simplifier<>(p -> new Vector(p.dist(), p.ele(), 0.0));

    public Climbs getClimbs(GPXPath gpxPath) {
        return getClimbs(gpxPath, 10.0, 35.0, 100.0, 3.0, 1.3, 1.3);
    }

    public Climbs getClimbs(
            GPXPath gpxPath,
            double minMinClimbElevation,
            double maxMinClimbElevation,
            double minClimbElevationRatio,
            double minGrade,
            double maxDiffRealGrade,
            double booster) {
        return new Climbs(detectClimbs(
                        gpxPath,
                        minMinClimbElevation,
                        maxMinClimbElevation,
                        minClimbElevationRatio,
                        minGrade,
                        maxDiffRealGrade,
                        booster)
                .stream()
                .map(detectedClimb -> getClimb(gpxPath, detectedClimb))
                .toList());
    }

    List<DetectedClimb> detectClimbs(
            GPXPath gpxPath,
            double minMinClimbElevation,
            double maxMinClimbElevation,
            double minClimbElevationRatio,
            double minGrade,
            double maxDiffRealGrade,
            double booster) {

        List<DetectedClimb> climbs = new ArrayList<>();
        int count = gpxPath.getPoints().size();

        double minClimbElevation = Math.max(
                minMinClimbElevation,
                Math.min(maxMinClimbElevation, gpxPath.getTotalElevation() / minClimbElevationRatio));

        for (int i = 0; i < count; i++) {
            // get best climb candidate for each point
            DetectedClimb climb =
                    getBestClimb(gpxPath, count, i, minClimbElevation, minGrade, maxDiffRealGrade, booster);
            if (climb != null) {
                climbs.add(climb);
            }
        }

        // sort by score descending
        Comparator<DetectedClimb> comparator = Comparator.comparing(DetectedClimb::score);
        climbs.sort(comparator.reversed());

        List<DetectedClimb> result = new ArrayList<>();
        // still at least one valid climb
        while (!climbs.isEmpty()) {
            // best climb left
            DetectedClimb climb = climbs.get(0);
            int i = climb.i();
            int j = climb.j();
            result.add(climb);

            // remove overlapping climbs
            climbs.removeIf(dc -> {
                int i1 = dc.i();
                int j1 = dc.j();

                if (i <= i1 && i1 <= j) {
                    return true;
                }
                if (i <= j1 && j1 <= j) {
                    return true;
                }
                return i1 <= i && j <= j1;
            });
        }

        // sort climbs by index order (distance)
        result.sort(Comparator.comparing(DetectedClimb::i));

        return result;
    }

    private DetectedClimb getBestClimb(
            GPXPath gpxPath,
            int count,
            int i,
            double minClimbElevation,
            double minGrade,
            double maxDiffRealGrade,
            double booster) {
        // at least minClimbElevation meters of difference of elevation from start to end
        double bestScore = 0.0;
        DetectedClimb bestClimb = null;

        double startDist = gpxPath.getDists()[i];
        double startEle = gpxPath.getEles()[i];
        // total positive elevation from i
        double positiveElevation = 0.0;
        // distance climbing from i
        double distClimbing = 0.0;
        // total negative elevation from i
        double negativeElevation = 0.0;

        // test all points
        for (int j = i + 1; j < count; j++) {
            double endDist = gpxPath.getDists()[j];
            double distance = endDist - startDist;

            double endEle = gpxPath.getEles()[j];
            // difference of elevation since previous point
            double ddele = endEle - gpxPath.getEles()[j - 1];
            if (ddele > 0) {
                // climbing
                positiveElevation += ddele;
                distClimbing += endDist - gpxPath.getDists()[j - 1];
            } else {
                // descending
                negativeElevation += ddele;
            }

            // at least 10m
            if (distance > 10) {
                // difference of elevation from start to end
                double adele = endEle - startEle;
                // average grade
                double grade = (100 * adele) / distance;

                // average grade with only climbs
                double climbingGrade;
                if (distClimbing > 0) {
                    climbingGrade = (100 * positiveElevation) / distClimbing;
                } else {
                    climbingGrade = 0.0;
                }

                double score = distance * Math.pow(grade, booster);

                // average grade should be at least 3%
                // more elevation than before
                // not too much descent (7% in real climbing with a 5% average climb is not ok)
                // climb will be split in two parts
                if (grade >= minGrade
                        && adele >= minClimbElevation
                        && score >= bestScore
                        && climbingGrade / grade <= maxDiffRealGrade) {
                    // new best
                    bestScore = score;
                    bestClimb = new DetectedClimb(
                            i,
                            j,
                            score,
                            startDist,
                            startEle,
                            endDist,
                            endEle,
                            endDist - startDist,
                            endEle - startEle,
                            positiveElevation,
                            negativeElevation,
                            grade,
                            climbingGrade);
                }
            }
        }
        return bestClimb;
    }

    private Climb getClimb(GPXPath gpxPath, DetectedClimb detectedClimb) {
        ClimbParts parts = getParts(gpxPath, detectedClimb);
        return new Climb(
                detectedClimb.startDist(),
                detectedClimb.startEle(),
                detectedClimb.endDist(),
                detectedClimb.endEle(),
                detectedClimb.dist(),
                detectedClimb.elevation(),
                detectedClimb.positiveElevation(),
                detectedClimb.negativeElevation(),
                detectedClimb.grade(),
                detectedClimb.climbingGrade(),
                parts);
    }

    private ClimbParts getParts(GPXPath gpxPath, DetectedClimb detectedClimb) {
        double initialDist = gpxPath.getDists()[detectedClimb.i()];
        List<ClimbPoint> points = getClimbPoints(gpxPath, detectedClimb, initialDist);
        return getClimbParts(points);
    }

    private List<ClimbPoint> getClimbPoints(GPXPath gpxPath, DetectedClimb detectedClimb, double initialDist) {
        List<ClimbPoint> points = new ArrayList<>();
        for (int i = detectedClimb.i(); i <= detectedClimb.j(); i++) {
            double dist = gpxPath.getDists()[i] - initialDist;
            double ele = gpxPath.getEles()[i];
            points.add(new ClimbPoint(dist, ele));
        }
        double tolerance = detectedClimb.elevation() / 50;
        tolerance = Math.max(10, Math.min(50, tolerance));
        points = simplifier.douglasPeucker(points, tolerance);

        return points;
    }

    private ClimbParts getClimbParts(List<ClimbPoint> points) {
        ClimbParts parts = new ClimbParts();
        for (int i = 0; i < points.size() - 1; i++) {
            double startDist = points.get(i).dist();
            double startEle = points.get(i).ele();

            double endDist = points.get(i + 1).dist();
            double endEle = points.get(i + 1).ele();

            double elevation = endEle - startEle;

            parts.add(new ClimbPart(
                    startDist,
                    startEle,
                    endDist,
                    endEle,
                    endDist - startDist,
                    elevation,
                    100 * elevation / (endDist - startDist)));
        }
        return parts;
    }
}
