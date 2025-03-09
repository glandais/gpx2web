package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.FitFileWriter;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.JsonFileWriter;
import io.github.glandais.gpx.io.write.tabular.XLSXFileWriter;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.virtual.maxspeed.MaxSpeedComputerTest;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderNone;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
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

    @Inject
    JsonFileWriter jsonFileWriter;

    @Inject
    FitFileWriter fitFileWriter;

    @SneakyThrows
    @Test
    @Disabled
    public void virtualizeTest() {
        Constants.DEBUG = true;

        String file = "/NP_591_G4C1.gpx";
        String output = "stelvio";

        new File("output").mkdirs();

        GPX gpx = new GPXFileReader().parseGpx(MaxSpeedComputerTest.class.getResourceAsStream(file));
        GPXPath gpxPath = gpx.paths().get(0);
        Cyclist cyclist = Cyclist.getDefault();
        cyclist.setHarmonics(true);
        Bike bike = Bike.getDefault();
        Course course = new Course(gpxPath, Instant.now(), cyclist, bike, new PowerProviderConstant(), new WindProviderNone(), new AeroProviderConstant());

        gpxPathEnhancer.virtualize(course, false);
        gpxFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".gpx"), true);
//        xlsxFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".xlsx"));
        jsonFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".json"));
        fitFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".fit"));

        Constants.DEBUG = false;
    }
}
