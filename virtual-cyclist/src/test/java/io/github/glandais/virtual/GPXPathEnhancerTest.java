package io.github.glandais.virtual;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.io.write.GPXFileWriter;
import io.github.glandais.io.write.tabular.XLSXFileWriter;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.aero.aero.AeroProviderConstant;
import io.github.glandais.virtual.aero.wind.WindProviderNone;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;

@QuarkusTest
public class GPXPathEnhancerTest {

    @Inject
    GPXPathEnhancer gpxPathEnhancer;

    @Inject
    GPXFileWriter gpxFileWriter;

    @Inject
    XLSXFileWriter xlsxFileWriter;

    @SneakyThrows
    @Test
    @Disabled
    public void virtualizeTest() {
        Constants.DEBUG = true;

        String file = "/stelvio.gpx";
        String output = "stelvio";

        new File("output").mkdirs();

        GPX gpx = new GPXFileReader().parseGpx(MaxSpeedComputerTest.class.getResourceAsStream(file));
        GPXPath gpxPath = gpx.paths().get(0);
        Cyclist cyclist = Cyclist.getDefault();
        Bike bike = Bike.getDefault();
        Course course = new Course(gpxPath, Instant.now(), cyclist, bike, new PowerProviderConstant(), new WindProviderNone(), new AeroProviderConstant());
        gpxPathEnhancer.virtualize(course, false);
        gpxFileWriter.writeGpxFile(gpx, new File("output/" + output + ".gpx"));
        xlsxFileWriter.writeXlsxFile(gpxPath, new File("output/" + output + ".xlsx"));

        Constants.DEBUG = false;
    }
}
