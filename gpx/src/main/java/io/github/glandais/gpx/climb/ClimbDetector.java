package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.GPXPath;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Singleton
public class ClimbDetector {

    private final ClimbPartDetector climbPartDetector = new ClimbPartDetector();

    public List<Climb> getClimbs(GPXPath gpxPath) {
        return detectClimbs(gpxPath)
                .stream()
                .map(detectedClimb -> getClimb(gpxPath, detectedClimb))
                .toList();
    }

    List<DetectedClimb> detectClimbs(GPXPath gpxPath) {

        List<DetectedClimb> climbs = new ArrayList<>();
        int count = gpxPath.getPoints().size();

        double minClimbElevation =
                Math.max(
                        10,
                        Math.min(
                                35,
                                gpxPath.getTotalElevation() / 100
                        )
                );

        for (int i = 0; i < count; i++) {
            // get best climb candidate for each point
            DetectedClimb climb = getBestClimb(gpxPath, count, i, minClimbElevation);
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

    private DetectedClimb getBestClimb(GPXPath gpxPath, int count, int i, double minClimbElevation) {
        // at least minClimbElevation meters of difference of elevation from start to end
        double bestScore = minClimbElevation;
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

                // average grade should be at least 3%
                // more elevation than before
                // not too much descent (7% in real climbing with a 5% average climb is not ok)
                //   climb will be split in two parts
                if (grade >= 3.0 && adele >= bestScore && climbingGrade / grade < 1.2) {
                    // new best
                    bestScore = adele;
                    bestClimb = new DetectedClimb(
                            i,
                            j,
                            adele,
                            startDist,
                            startEle,
                            endDist,
                            endEle,
                            endDist - startDist,
                            endEle - startEle,
                            positiveElevation,
                            negativeElevation,
                            grade,
                            climbingGrade
                    );
                }
            }
        }
        return bestClimb;
    }

    private Climb getClimb(GPXPath gpxPath, DetectedClimb detectedClimb) {
        List<ClimbPart> parts = climbPartDetector.getParts(gpxPath, detectedClimb);
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
                parts
        );
    }

}