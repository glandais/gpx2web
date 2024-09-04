package io.github.glandais.gpx.climb;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerDistance;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.srtm.SRTMHelper;
import io.github.glandais.util.SmoothService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

class ClimbDetectorTest {

    @SneakyThrows
    @Test
    void getClimbs() {
        GPXParser gpxParser = new GPXParser();
        GPXElevationFixer gpxElevationFixer = new GPXElevationFixer(
                new SRTMHelper(() -> new File("/tmp")),
                new GPXPerDistance(),
                new SmoothService()
        );
        ClimbDetector climbDetector = new ClimbDetector();

//        getClimbs(gpxParser, "/test.gpx", gpxElevationFixer, climbDetector);
//        getClimbs(gpxParser, "/test2.gpx", gpxElevationFixer, climbDetector);
//        getClimbs(gpxParser, "/test3.gpx", gpxElevationFixer, climbDetector);
        getClimbs(gpxParser, "/ventoux.gpx", gpxElevationFixer, climbDetector);
    }

    void getClimbs(GPXParser gpxParser, String file, GPXElevationFixer gpxElevationFixer, ClimbDetector climbDetector) throws Exception {
        List<GPXPath> gpxPaths = gpxParser.parsePaths(ClimbDetectorTest.class.getResourceAsStream(file));
        GPXPath gpxPath = gpxPaths.get(0);

        gpxElevationFixer.fixElevation(gpxPath, true);

        for (int i = 0; i < gpxPath.getPoints().size(); i++) {
            System.out.println(i + " " + gpxPath.getDists()[i] + " " + gpxPath.getEles()[i] + " " + (100*gpxPath.getPoints().get(i).getGrade()));
        }

        List<Climb> climbs = climbDetector.getClimbs(gpxPath);
        for (Climb climb : climbs) {
            System.out.println(climb);
        }
    }

}
