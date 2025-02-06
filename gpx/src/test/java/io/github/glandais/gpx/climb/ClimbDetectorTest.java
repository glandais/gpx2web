package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.GPXPerDistance;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.srtm.GpxElevationProvider;
import io.github.glandais.util.SmoothService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

class ClimbDetectorTest {

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

        double score = totl * Math.pow(totg, b);
        return score;
    }

    @SneakyThrows
    @Test
    @Disabled
    void getClimbs() {
        GPXFileReader gpxFileReader = new GPXFileReader();
        GPXElevationFixer gpxElevationFixer = new GPXElevationFixer(
                new GpxElevationProvider(() -> new File("/tmp")),
                new GPXPerDistance(),
                new SmoothService()
        );
        ClimbDetector climbDetector = new ClimbDetector();

//        getClimbs(gpxParser, "/test.gpx", gpxElevationFixer, climbDetector);
//        getClimbs(gpxParser, "/test2.gpx", gpxElevationFixer, climbDetector);
//        getClimbs(gpxParser, "/test3.gpx", gpxElevationFixer, climbDetector);
        getClimbs(gpxFileReader, "/ventoux.gpx", gpxElevationFixer, climbDetector);
//        getClimbs(gpxParser, "/Etape_36.gpx", gpxElevationFixer, climbDetector);
    }

    void getClimbs(GPXFileReader gpxFileReader, String file, GPXElevationFixer gpxElevationFixer, ClimbDetector climbDetector) throws Exception {
        List<GPXPath> gpxPaths = gpxFileReader.parseGpx(ClimbDetectorTest.class.getResourceAsStream(file)).paths();
        GPXPath gpxPath = gpxPaths.get(0);

        gpxElevationFixer.fixElevation(gpxPath, true);

        for (int i = 0; i < gpxPath.getPoints().size(); i++) {
            System.out.println(i + " " + gpxPath.getDists()[i] + " " + gpxPath.getEles()[i] + " " + (100 * gpxPath.getPoints().get(i).getGrade()));
        }

        List<Climb> climbs = climbDetector.getClimbs(gpxPath);
        for (Climb climb : climbs) {
            System.out.println(climb);
        }
    }

}
