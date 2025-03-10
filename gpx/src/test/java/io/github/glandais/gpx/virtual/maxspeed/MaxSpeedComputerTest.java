package io.github.glandais.gpx.virtual.maxspeed;


import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKey;
import io.github.glandais.gpx.data.values.unit.StorageUnit;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.util.Constants;
import io.github.glandais.gpx.virtual.Bike;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.Cyclist;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderNone;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
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

        Cyclist cyclist = Cyclist.getDefault();
        Bike bike = Bike.getDefault();
        Course course = new Course(gpxPath, Instant.now(), cyclist, bike, new PowerProviderConstant(), new WindProviderNone(), new AeroProviderConstant());

        MaxSpeedComputer maxSpeedComputer = new MaxSpeedComputer();

        maxSpeedComputer.firstPass(course);
        maxSpeedComputer.secondPass(course);

        for (Point point : course.getGpxPath().getPoints()) {

            String maxSpeed = get(point, ValueKey.speed_max, Unit.SPEED_S_M);
            String vmaxIncline = get(point, ValueKey.speed_max_incline, Unit.SPEED_S_M);
            if (maxSpeed.equalsIgnoreCase(vmaxIncline)) {
                System.out.print("*** ");
            }
            System.out.println("dist : " + get(point, ValueKey.dist, Unit.METERS) + " - " +
                    "speed_max : " + maxSpeed + " - " +
                    "radius : " + get(point, ValueKey.radius, Unit.METERS) + " - " +
                    "speed_max_incline : " + vmaxIncline);
        }

        Constants.DEBUG = false;
    }

    public <J> String get(Point point, ValueKey key, StorageUnit<J> unit) {
        J value = point.get(key, unit);
        if (value != null) {
            return unit.formatHuman(value);
        } else {
            return "null";
        }
    }

}
