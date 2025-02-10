package io.github.glandais.virtual;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.wind.WindProviderNone;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@QuarkusTest
public class GPXPathEnhancerTest {

    @Inject
    GPXPathEnhancer gpxPathEnhancer;

    @SneakyThrows
    @Test
    @Disabled
    public void virtualizeTest() {
        Constants.DEBUG = true;

        GPXPath gpxPath = new GPXFileReader().parseGpx(MaxSpeedComputerTest.class.getResourceAsStream("/stelvio.gpx")).paths().get(0);

        Cyclist cyclist = new Cyclist();
        Course course = new Course(gpxPath, Instant.now(), cyclist, new PowerProviderConstant(), new WindProviderNone(), new CxProviderConstant());

        gpxPathEnhancer.virtualize(course, false);

        Constants.DEBUG = false;
    }
}
