package io.github.glandais.process;

import io.github.glandais.GpxCommand;
import io.github.glandais.virtual.Cyclist;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.ZonedDateTime;


@Data
@Slf4j
@Component
@Command(name = "process", mixinStandardHelpOptions = true)
public class ProcessCommand extends GpxCommand {

    @Autowired
    private GpxProcessor gpxProcessor;

    @Option(names = {"--map-srtm"}, negatable = true, description = "Elevation map")
    private boolean srtmMap = false;

    @Option(names = {"--map"}, negatable = true, description = "Map")
    private boolean tileMap = false;

    @Option(names = {"--map-tile-url"}, description = "Map tile URL")
    private String tileUrl = "https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png";

    @Option(names = {"--map-width"}, description = "Map width")
    private int width = 1024;

    @Option(names = {"--map-height"}, description = "Map height")
    private int height = 768;

    @Option(names = {"--chart"}, negatable = true, description = "Chart")
    private boolean chart = true;

    @Option(names = {"--kml"}, negatable = true, description = "Output KML file")
    private boolean kml = false;

    @Option(names = {"--fit"}, negatable = true, description = "Output FIT file")
    private boolean fit = false;

    @Option(names = {"--fix-elevation"}, negatable = true, description = "Fix elevation")
    private boolean fixElevation = true;

    @Option(names = {"--second-precision"}, negatable = true, description = "Create a point per second")
    private boolean pointPerSecond = true;

    @Option(names = {"--filter"}, negatable = true, description = "Filter output")
    private boolean filter = true;

    @Option(names = {"--virtual-time"}, negatable = true, description = "Simulate a cyclist")
    private boolean virtualTime = true;

    @Option(names = {"--simple-virtual-speed"}, description = "Cyclist speed (km/h)")
    private Double simpleVirtualSpeed = null;

    @Option(names = {"--cyclist-weight"}, description = "Cyclist weight with bike (kg)")
    private double mKg = 80;

    @Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    private double powerW = 240;

    @Option(names = {"--cyclist-max-angle"}, description = "Cyclist max angle (°)")
    private double maxAngleDeg = 15;

    @Option(names = {"--cyclist-max-speed"}, description = "Cyclist max speed (km/h)")
    private double maxSpeedKmH = 90;

    @Option(names = {"--cyclist-max-brake"}, description = "Cyclist max brake (g)")
    private double maxBrakeG = 0.3;

    // m.s-2
    @Option(names = {"--wind-speed"}, description = "Wind speed (m.s-2)")
    private double windSpeed = 0.0;

    @Option(names = {"--wind-direction"}, description = "Wind direction (°, clockwise, 0=N)")
    private double windDirectionDegree = 0.0;

    // rad (0 = N, Pi/2 = E)
    private double windDirection;

    private ZonedDateTime[] starts;
    private int counter = 0;
    private int dday = 0;

    private Cyclist cyclist;

    public ZonedDateTime getNextStart() {
        if (counter < starts.length) {
            ZonedDateTime start = starts[counter];
            counter++;
            return start;
        } else {
            dday++;
            return starts[starts.length - 1].plusDays(1);
        }
    }

    @SneakyThrows
    @Override
    public void run() {

        super.run();

        starts = new ZonedDateTime[1];
        ZonedDateTime start = ZonedDateTime.now();
        start = start.minusYears(1);
        starts[0] = start;

//        System.setProperty("gpx.data.cache", cacheValue);
        cyclist = new Cyclist(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG);
        windDirection = Math.toRadians(windDirectionDegree);
        for (File gpxFile : gpxFiles) {
            gpxProcessor.process(gpxFile, this);
        }
    }
}
