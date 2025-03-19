package io.github.glandais.virtualize;

import io.github.glandais.BikeMixin;
import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.gpx.filter.GPXPerDistance;
import io.github.glandais.gpx.filter.GPXPerSecond;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.tabular.CSVFileWriter;
import io.github.glandais.gpx.srtm.GPXElevationFixer;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.VirtualizeService;
import io.github.glandais.gpx.virtual.maxspeed.MaxSpeedComputer;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProvider;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.Wind;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProvider;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderConstant;
import io.github.glandais.gpx.virtual.power.cyclist.CyclistPowerProvider;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import jakarta.inject.Inject;
import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Slf4j
@Command(name = "virtualize", mixinStandardHelpOptions = true)
public class VirtualizeCommand implements Runnable {

    @Inject
    protected GPXFileReader gpxFileReader;

    @Inject
    protected GPXPerDistance gpxPerDistance;

    @Inject
    protected GPXElevationFixer gpxElevationFixer;

    @Inject
    protected MaxSpeedComputer maxSpeedComputer;

    @Inject
    protected VirtualizeService virtualizeService;

    @Inject
    protected GPXPerSecond gpxPerSecond;

    @Inject
    protected GPXFileWriter gpxFileWriter;

    @Inject
    protected CSVFileWriter csvFileWriter;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @CommandLine.Mixin
    protected BikeMixin bikeMixin;

    @Option(
            names = {"--csv"},
            negatable = true,
            description = "Output CSV file")
    protected boolean csv = false;

    // m.s-2
    @Option(
            names = {"--wind-speed"},
            description = "Wind speed (km/s)")
    protected double windSpeedKmH = 0.0;

    @Option(
            names = {"--wind-direction"},
            description = "Wind direction (°, clockwise, 0=N)")
    protected double windDirectionDegree = 0.0;

    @Option(
            names = {"--start"},
            description = "Start date ISO8601")
    protected Instant startDate =
            ZonedDateTime.now().withHour(7).withMinute(0).minusYears(1).toInstant();

    protected WindProvider windProvider;
    protected AeroProvider aeroProvider;
    protected CyclistPowerProvider powerProvider;

    protected Instant[] starts;
    protected int counter = 0;
    protected int dday = 0;

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
        cyclistMixin.initCyclist();
        init();

        filesMixin.processFiles(gpxFileReader, this::process);
    }

    protected void init() {
        starts = new Instant[1];
        starts[0] = startDate;

        windProvider = new WindProviderConstant(new Wind(windSpeedKmH / 3.6, Math.toRadians(windDirectionDegree)));
        aeroProvider = new AeroProviderConstant();
        powerProvider = new PowerProviderConstant();
    }

    @SneakyThrows
    private void process(GPXPath path, File pathFolder) {

        GPXFilter.filterPointsDouglasPeucker(path);
        gpxPerDistance.computeOnePointPerDistance(path, 10);
        gpxElevationFixer.fixElevation(path);
        GPXFilter.filterPointsDouglasPeucker(path);

        log.info("D+ : {} m", path.getTotalElevation());

        Instant start = getNextStart();

        Course course = new Course(
                path, start, cyclistMixin.getCyclist(), bikeMixin.getBike(), powerProvider, windProvider, aeroProvider);
        maxSpeedComputer.computeMaxSpeeds(course);
        virtualizeService.virtualizeTrack(course);

        gpxPerSecond.computeOnePointPerSecond(path);
        GPXFilter.filterPointsDouglasPeucker(path);

        log.info("Writing GPX for {}", path.getName());
        gpxFileWriter.writeGPXPath(path, new File(pathFolder, path.getName() + ".gpx"), true);

        if (csv) {
            log.info("Writing CSV for path {}", path.getName());
            csvFileWriter.writeGPXPath(path, new File(pathFolder, path.getName() + ".csv"));
        }
    }
}
