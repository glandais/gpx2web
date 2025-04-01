package io.github.glandais.gpx.virtual;

import io.github.glandais.gpx.Context;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderNone;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import java.io.File;
import java.time.Instant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class GPXEnhancerTest {

    @SneakyThrows
    @Test
    public void virtualizeTest() {
        Constants.DEBUG = true;

        String file = "/ventoux.gpx";
        String output = "output";

        File outputFolder = new File("../static");
        System.out.println(outputFolder.getAbsolutePath());
        outputFolder.mkdirs();

        GPX gpx = new GPXFileReader().parseGPX(GPXEnhancerTest.class.getResourceAsStream(file));
        GPXPath gpxPath = gpx.paths().get(0);
        Point point = gpxPath.getPoints().get(0);
        Instant instant = Context.INSTANCE.getStartTimeProvider().getStart(point, -80);
        Cyclist cyclist = Cyclist.getDefault();
        cyclist.setHarmonics(true);
        Bike bike = Bike.getDefault();
        Course course = new Course(
                gpxPath,
                instant,
                cyclist,
                bike,
                new PowerProviderConstant(),
                new WindProviderNone(),
                new AeroProviderConstant());

        long now = System.currentTimeMillis();
        Context.INSTANCE.getGpxEnhancer().virtualize(course, false);
        System.out.println(System.currentTimeMillis() - now);
        Context.INSTANCE.getGpxFileWriter().writeGPXPath(gpxPath, new File("target/" + output + ".gpx"), false);
        Context.INSTANCE.getJsonFileWriter().writeGPXPath(gpxPath, new File(outputFolder, output + ".json"));
        Context.INSTANCE.getFitFileWriter().writeGPXPath(gpxPath, new File("target/" + output + ".fit"));

        Constants.DEBUG = false;
    }
}
