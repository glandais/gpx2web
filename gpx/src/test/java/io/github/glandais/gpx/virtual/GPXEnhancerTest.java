package io.github.glandais.gpx.virtual;

import eu.lestard.easydi.EasyDI;
import io.github.glandais.gpx.CacheFolderProviderImpl;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.FitFileWriter;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.JsonFileWriter;
import io.github.glandais.gpx.util.CacheFolderProvider;
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
    @Disabled
    public void virtualizeTest() {
        Constants.DEBUG = true;

        EasyDI easyDI = new EasyDI();
        easyDI.bindInterface(CacheFolderProvider.class, CacheFolderProviderImpl.class);
        GPXEnhancer gpxEnhancer = easyDI.getInstance(GPXEnhancer.class);
        GPXFileWriter gpxFileWriter = easyDI.getInstance(GPXFileWriter.class);
        JsonFileWriter jsonFileWriter = easyDI.getInstance(JsonFileWriter.class);
        FitFileWriter fitFileWriter = easyDI.getInstance(FitFileWriter.class);
        StartTimeProvider startTimeProvider = easyDI.getInstance(StartTimeProvider.class);

        //        PowerComputer powerComputer = easyDI.getInstance(PowerComputer.class);
        //        double dxToNext = 0.23629331788056263;
        //        double currentSpeed = 6.152075339127345;
        //        double pSum = 630.9136101780555;
        //        double equivalentMass = 80.24489795918367;
        //        double dtToNext = powerComputer.getDt(pSum, equivalentMass, currentSpeed, dxToNext);
        //        double dx = powerComputer.getDx(pSum, equivalentMass, currentSpeed, dtToNext);
        //        System.out.println(dxToNext - dx);
        //        double newSpeed = 2 * (dxToNext / dtToNext) - currentSpeed;
        //        dtToNext = 2 * dxToNext / (currentSpeed + newSpeed);
        //
        //        double totPower = powerComputer.getTotPower(equivalentMass, currentSpeed, newSpeed, dtToNext);
        //        System.out.println(pSum - totPower);

        String file = "/AMR.gpx";
        String output = "stelvio";

        new File("output").mkdirs();

        GPX gpx = new GPXFileReader().parseGPX(GPXEnhancerTest.class.getResourceAsStream(file));
        GPXPath gpxPath = gpx.paths().get(0);
        Point point = gpxPath.getPoints().get(0);
        Instant instant = startTimeProvider.getStart(point, -80);
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
        gpxEnhancer.virtualize(course, false);
        System.out.println(System.currentTimeMillis() - now);
        gpxFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".gpx"), false);
        jsonFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".json"));
        fitFileWriter.writeGPXPath(gpxPath, new File("output/" + output + ".fit"));

        Constants.DEBUG = false;
    }
}
