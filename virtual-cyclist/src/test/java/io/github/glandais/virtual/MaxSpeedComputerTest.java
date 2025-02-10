package io.github.glandais.virtual;


import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.unit.StorageUnit;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.util.Constants;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.wind.WindProviderNone;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class MaxSpeedComputerTest {

    @SneakyThrows
    @Test
    @Disabled
    public void computeMaxSpeedsTest() {
        Constants.DEBUG = true;

        GPXPath gpxPath = new GPXFileReader().parseGpx(MaxSpeedComputerTest.class.getResourceAsStream("/stelvio.gpx")).paths().get(0);

        Cyclist cyclist = new Cyclist();
        Course course = new Course(gpxPath, Instant.now(), cyclist, new PowerProviderConstant(), new WindProviderNone(), new CxProviderConstant());

        MaxSpeedComputer maxSpeedComputer = new MaxSpeedComputer();

        maxSpeedComputer.firstPass(course);
        maxSpeedComputer.secondPass(course);

        for (Point point : course.getGpxPath().getPoints()) {

            String maxSpeed = get(point, "max_speed", Unit.SPEED_S_M);
            String vmaxIncline = get(point, "vmax_incline", Unit.SPEED_S_M);
            if (maxSpeed.equalsIgnoreCase(vmaxIncline)) {
                System.out.print("*** ");
            }
            System.out.println("dist : " + get(point, "dist", Unit.METERS) + " - " +
                    "max_speed : " + maxSpeed + " - " +
                    "radius : " + get(point, "radius", Unit.METERS) + " - " +
                    "vmax_incline : " + vmaxIncline);
        }

        Constants.DEBUG = false;
    }

    public <J> String get(Point point, String key, StorageUnit<J> unit) {
        J value = point.get(key, unit);
        if (value != null) {
            return unit.formatHuman(value);
        } else {
            return "null";
        }
    }

}
