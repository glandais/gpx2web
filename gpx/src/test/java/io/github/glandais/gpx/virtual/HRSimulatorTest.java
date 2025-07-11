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
            GPX gpxHr = gpxFileReader.parseGPX(GPXEnhancerTest.class.getResourceAsStream("/hr/hr" + i + ".gpx"));
            GPXPath gpxPathHr = gpxHr.paths().get(0);
            hrPaths.add(gpxPathHr);
        }
        new HRSimulator(new SmoothService()).train(hrPaths);
    }

    @Test
    @SneakyThrows
    void test() {
        GPXFileReader gpxFileReader = new GPXFileReader();
        for (int i = 1; i <= 7; i++) {
            GPX gpxHr = gpxFileReader.parseGPX(GPXEnhancerTest.class.getResourceAsStream("/hr/hr" + i + ".gpx"));
            GPXPath path = gpxHr.paths().get(0);
            Context.INSTANCE.getGpxFileWriter().writeGPXPath(path, new File("target/hr-orig-" + i + ".gpx"), true);
            for (Point point : path.getPoints()) {
                point.setHeartRate(null);
            }
            new HRSimulator(new SmoothService()).simulateHeartRate(path);
            Context.INSTANCE.getGpxFileWriter().writeGPXPath(path, new File("target/hr-guess-" + i + ".gpx"), true);
        }
    }
}
