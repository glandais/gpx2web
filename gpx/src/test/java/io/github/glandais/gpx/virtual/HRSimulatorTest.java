package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.Context;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.util.SmoothService;
import io.github.glandais.gpx.virtual.heartrate.HRSimulator;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class HRSimulatorTest {

    @Test
    @Disabled
    @SneakyThrows
    void train() {
        GPXFileReader gpxFileReader = new GPXFileReader();

        List<GPXPath> hrPaths = new ArrayList<>();
        for (int i = 1; i <= 7; i++) {
            hrPaths.add(getGpxPath(gpxFileReader, "/hr/hr" + i + ".gpx"));
        }
        new HRSimulator(new SmoothService()).train(hrPaths);
    }

    private static GPXPath getGpxPath(GPXFileReader gpxFileReader, String fileName) throws Exception {
        GPX gpxHr = gpxFileReader.parseGPX(GPXEnhancerTest.class.getResourceAsStream(fileName));
        return gpxHr.paths().get(0);
    }

    @Test
    @SneakyThrows
    void test() {
        GPXFileReader gpxFileReader = new GPXFileReader();
        for (int i = 1; i <= 7; i++) {
            String fileName = "/hr/hr" + i + ".gpx";
            GPXPath path = getGpxPath(gpxFileReader, fileName);
            Context.INSTANCE.getGpxFileWriter().writeGPXPath(path, new File("target/hr-orig-" + i + ".gpx"), true);
            for (Point point : path.getPoints()) {
                point.setHeartRate(null);
            }
            new HRSimulator(new SmoothService()).simulateHeartRate(path);
            Context.INSTANCE.getGpxFileWriter().writeGPXPath(path, new File("target/hr-guess-" + i + ".gpx"), true);
        }
    }
}
