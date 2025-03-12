package io.github.glandais.gpx.virtual;

import eu.lestard.easydi.EasyDI;
import io.github.glandais.gpx.CacheFolderProviderImpl;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.FitFileWriter;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.JsonFileWriter;
import io.github.glandais.gpx.io.write.tabular.XLSXFileWriter;
import io.github.glandais.gpx.util.CacheFolderProvider;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderNone;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;

class GPXPathEnhancerTest {

    @SneakyThrows
    @Test
    @Disabled
    public void virtualizeTest() {
        Constants.DEBUG = true;

        EasyDI easyDI = new EasyDI();
        easyDI.bindInterface(CacheFolderProvider.class, CacheFolderProviderImpl.class);
        GPXPathEnhancer gpxPathEnhancer = easyDI.getInstance(GPXPathEnhancer.class);
        GPXFileWriter gpxFileWriter = easyDI.getInstance(GPXFileWriter.class);
        XLSXFileWriter xlsxFileWriter = easyDI.getInstance(XLSXFileWriter.class);
        JsonFileWriter jsonFileWriter = easyDI.getInstance(JsonFileWriter.class);
        FitFileWriter fitFileWriter = easyDI.getInstance(FitFileWriter.class);

        String file = "/AMR.gpx";
        String output = "stelvio";

        new File("output").mkdirs();

        GPX gpx = new GPXFileReader().parseGpx(GPXPathEnhancerTest.class.getResourceAsStream(file));
        GPXPath gpxPath = gpx.paths().get(0);
        Cyclist cyclist = Cyclist.getDefault();
        cyclist.setHarmonics(true);
        Bike bike = Bike.getDefault();
        Course course = new Course(gpxPath, Instant.now(), cyclist, bike, new PowerProviderConstant(), new WindProviderNone(), new AeroProviderConstant());

        long now = System.currentTimeMillis();
        gpxPathEnhancer.virtualize(course, false);
        System.out.println(System.currentTimeMillis() - now);
        gpxFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".gpx"), true);
//        xlsxFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".xlsx"));
        jsonFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".json"));
        fitFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".fit"));

        Constants.DEBUG = false;
    }
}
