package io.github.glandais.virtualize;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.io.CSVFileWriter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.aero.cx.CxProvider;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.wind.Wind;
import io.github.glandais.virtual.aero.wind.WindProvider;
import io.github.glandais.virtual.aero.wind.WindProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import jakarta.inject.Inject;
import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;

@Slf4j
@Command(name = "virtualize", mixinStandardHelpOptions = true)
public class VirtualizeCommand implements Runnable {

    @Inject
    protected GPXParser gpxParser;

    @Inject
    protected GPXElevationFixer gpxElevationFixer;

    @Inject
    protected MaxSpeedComputer maxSpeedComputer;

    @Inject
    protected PowerComputer powerComputer;

    @Inject
    protected GPXPerSecond gpxPerSecond;

    @Inject
    protected GPXFileWriter gpxFileWriter;

    @Inject
    protected CSVFileWriter csvFileWriter;

//    @Inject
//    protected XLSXFileWriter xlsxFileWriter;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @Option(names = {"--csv"}, negatable = true, description = "Output CSV file")
    protected boolean csv = false;

//    @Option(names = {"--xlsx"}, negatable = true, description = "Output XLSX file")
//    protected boolean xlsx = false;

    @Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    protected double powerW = 240;

    @Option(names = {"--cyclist-cx"}, description = "Cyclist Cx")
    protected double cx = 0.3;

    // m.s-2
    @Option(names = {"--wind-speed"}, description = "Wind speed (km/s)")
    protected double windSpeedKmH = 0.0;

    @Option(names = {"--wind-direction"}, description = "Wind direction (°, clockwise, 0=N)")
    protected double windDirectionDegree = 0.0;

    protected WindProvider windProvider;
    protected CxProvider cxProvider;
    protected PowerProvider powerProvider;

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

        filesMixin.processFiles(gpxParser, this::process);
    }

    protected void init() {
        starts = new Instant[1];
        starts[0] = ZonedDateTime.now().withHour(7).withMinute(0).minusYears(1).toInstant();

//        System.setProperty("gpx.data.cache", cacheValue);
        windProvider = new WindProviderConstant(new Wind(windSpeedKmH / 3.6, Math.toRadians(windDirectionDegree)));
        cxProvider = new CxProviderConstant(cx);
        powerProvider = new PowerProviderConstant(powerW);
    }

    @SneakyThrows
    private void process(GPXPath path, File pathFolder) {

        GPXFilter.filterPointsDouglasPeucker(path);
        gpxElevationFixer.fixElevation(path, true);
        GPXFilter.filterPointsDouglasPeucker(path);

        log.info("D+ : {} m", path.getTotalElevation());

        Instant start = getNextStart();

        Course course = new Course(path, start,
                cyclistMixin.getCyclist(),
                powerProvider,
                windProvider,
                cxProvider);
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);

        gpxPerSecond.computeOnePointPerSecond(path);
        GPXFilter.filterPointsDouglasPeucker(path);

        log.info("Writing GPX for {}", path.getName());
        gpxFileWriter.writeGpxFile(Collections.singletonList(path), new File(pathFolder, path.getName() + ".gpx"), true);

        if (csv) {
            log.info("Writing CSV for path {}", path.getName());
            csvFileWriter.writeCsvFile(path, new File(pathFolder, path.getName() + ".csv"));
        }

//        if (xlsx) {
//            log.info("Writing XLSX for path {}", path.getName());
//            xlsxFileWriter.writeXlsxFile(path, new File(pathFolder, path.getName() + ".xlsx"));
//        }
    }

}
