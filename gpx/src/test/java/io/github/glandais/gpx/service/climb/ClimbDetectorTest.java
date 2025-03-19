package io.github.glandais.gpx.service.climb;

import io.github.glandais.gpx.climb.Climb;
import io.github.glandais.gpx.climb.ClimbDetector;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.filter.GPXPerDistance;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.srtm.GPXElevationFixer;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ClimbDetectorTest {

    @Inject
    GPXFileReader gpxFileReader;

    @Inject
    GPXPerDistance gpxPerDistance;

    @Inject
    GPXElevationFixer gpxElevationFixer;

    @Inject
    ClimbDetector climbDetector;

    @SneakyThrows
    @Test
    @Disabled
    void getScores() {
        List<Double> l = List.of(100.0, 300.0, 100.0);
        List<Double> e = List.of(1.0, 30.0, 1.0);

        for (int i = 0; i < 1000; i++) {
            double b = 1.0 + (i / 1000.0);
            double full = getScore(0, 2, l, e, b);
            double start = getScore(0, 1, l, e, b);
            double middle = getScore(1, 1, l, e, b);
            if (middle > full && middle > start) {
                System.out.println(b + " " + full + " " + middle + " " + start);
            }
        }
    }

    private static double getScore(int i, int j, List<Double> l, List<Double> e, double b) {
        double totl = 0;
        double tote = 0;
        for (int k = i; k <= j; k++) {
            totl = totl + l.get(k);
            tote = tote + e.get(k);
        }
        double totg = 100 * tote / totl;

        return totl * Math.pow(totg, b);
    }

    @SneakyThrows
    @Test
    @Disabled
    void getClimbs() {
        // getClimbs(gpxParser, "/test.gpx", gpxElevationFixer, climbDetector);
        // getClimbs(gpxParser, "/test2.gpx", gpxElevationFixer, climbDetector);
        // getClimbs(gpxParser, "/test3.gpx", gpxElevationFixer, climbDetector);
        getClimbs("/ventoux.gpx");
        // getClimbs(gpxParser, "/Etape_36.gpx", gpxElevationFixer, climbDetector);
    }

    void getClimbs(String file) throws Exception {
        List<GPXPath> gpxPaths = gpxFileReader
                .parseGPX(ClimbDetectorTest.class.getResourceAsStream(file))
                .paths();
        GPXPath gpxPath = gpxPaths.get(0);

        gpxPerDistance.computeOnePointPerDistance(gpxPath, 10);
        gpxElevationFixer.fixElevation(gpxPath);

        for (int i = 0; i < gpxPath.getPoints().size(); i++) {
            System.out.println(i
                    + " "
                    + gpxPath.getDists()[i]
                    + " "
                    + gpxPath.getEles()[i]
                    + " "
                    + (100 * gpxPath.getPoints().get(i).getGrade()));
        }

        List<Climb> climbs = climbDetector.getClimbs(gpxPath);
        for (Climb climb : climbs) {
            System.out.println(climb);
        }
    }
}
