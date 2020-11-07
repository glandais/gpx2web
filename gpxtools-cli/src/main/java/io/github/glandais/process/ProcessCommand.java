package io.github.glandais.process;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.virtual.wind.Wind;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;

@Data
@Slf4j
@Component
@Command(name = "process", mixinStandardHelpOptions = true)
public class ProcessCommand implements Runnable {

    @Autowired
    private GpxProcessor gpxProcessor;

    @Delegate
    @CommandLine.Mixin
    private FilesMixin filesMixin;

    @Delegate
    @CommandLine.Mixin
    private CyclistMixin cyclistMixin;

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

    @Option(names = {"--no-chart"}, negatable = true, description = "Chart")
    private boolean chart = true;

    @Option(names = {"--csv"}, negatable = true, description = "Output CSV file")
    private boolean csv = false;

    @Option(names = {"--kml"}, negatable = true, description = "Output KML file")
    private boolean kml = false;

    @Option(names = {"--fit"}, negatable = true, description = "Output FIT file")
    private boolean fit = false;

    @Option(names = {"--gpx-data"}, negatable = true, description = "Gpx has data (elevation, power, ...)")
    private boolean gpxData = false;

    @Option(names = {"--no-fix-elevation"}, negatable = true, description = "Fix elevation")
    private boolean fixElevation = true;

    @Option(names = {"--no-second-precision"}, negatable = true, description = "Create a point per second. Never applied for GPX with data")
    private boolean pointPerSecond = true;

    @Option(names = {"--no-simulate"}, negatable = true, description = "Simulate a cyclist")
    private boolean simulate = true;

    @Option(names = {"--gpx-power"}, negatable = true, description = "Reuse GPX power data")
    private boolean gpxPower = false;

    @Option(names = {"--simulate-speed"}, description = "Cyclist speed (km/h)\nDisable the computation using power, wind, etc")
    private Double simpleVirtualSpeed = null;

    @CommandLine.Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    private double powerW = 240;

    @CommandLine.Option(names = {"--cyclist-cx"}, description = "Cyclist Cx")
    double cx = 0.3;

    // m.s-2
    @Option(names = {"--wind-speed"}, description = "Wind speed (m.s-2)")
    private double windSpeed = 0.0;

    @Option(names = {"--wind-direction"}, description = "Wind direction (°, clockwise, 0=N)")
    private double windDirectionDegree = 0.0;

    private Wind wind;

    private Instant[] starts;
    private int counter = 0;
    private int dday = 0;

    public Instant getNextStart() {
        if (counter < starts.length) {
            Instant start = starts[counter];
            counter++;
            return start;
        } else {
            dday++;
            return starts[starts.length - 1].plusSeconds(3600 * 24);
        }
    }

    @SneakyThrows
    public void run() {
        initFiles();
        initCyclist();
        init();

        for (File gpxFile : filesMixin.getGpxFiles()) {
            gpxProcessor.process(gpxFile, this);
        }
    }

    private void init() {
        starts = new Instant[1];
        starts[0] = ZonedDateTime.now().minusYears(1).toInstant();

//        System.setProperty("gpx.data.cache", cacheValue);
        wind = new Wind(windSpeed, Math.toRadians(windDirectionDegree));
    }
}
