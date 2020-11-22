package io.github.glandais.process;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.GPXPerSecond;
import io.github.glandais.gpx.SimpleTimeComputer;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.io.CSVFileWriter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.io.XLSXFileWriter;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.SmoothService;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import io.github.glandais.virtual.PowerProvider;
import io.github.glandais.virtual.aero.cx.CxGuesser;
import io.github.glandais.virtual.aero.cx.CxProvider;
import io.github.glandais.virtual.aero.cx.CxProviderConstant;
import io.github.glandais.virtual.aero.cx.CxProviderFromData;
import io.github.glandais.virtual.aero.wind.Wind;
import io.github.glandais.virtual.aero.wind.WindProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import io.github.glandais.virtual.grav.WeightGuesser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@Command(name = "process", mixinStandardHelpOptions = true)
public class ProcessCommand implements Runnable {

    @Autowired
    protected GPXParser gpxParser;

    @Autowired
    protected GPXElevationFixer gpxElevationFixer;

    @Autowired
    protected SimpleTimeComputer simpleTimeComputer;

    @Autowired
    protected MaxSpeedComputer maxSpeedComputer;

    @Autowired
    protected PowerComputer powerComputer;

    @Autowired
    protected GPXPerSecond gpxPerSecond;

    @Autowired
    protected GPXFileWriter gpxFileWriter;

    @Autowired
    protected SmoothService smoothService;

    @Autowired
    protected CSVFileWriter csvFileWriter;

    @Autowired
    protected XLSXFileWriter xlsxFileWriter;

    @Autowired
    protected CxGuesser cxGuesser;

    @Autowired
    protected WeightGuesser weightGuesser;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @CommandLine.Option(names = {"-o", "--output"}, description = "Output folder")
    protected File output = new File("output");

    @Option(names = {"--csv"}, negatable = true, description = "Output CSV file")
    protected boolean csv = false;

    @Option(names = {"--xlsx"}, negatable = true, description = "Output XLSX file")
    protected boolean xlsx = false;

    @Option(names = {"--gpx-data"}, negatable = true, description = "Gpx has data (elevation, power, ...)")
    protected boolean gpxData = false;

    @Option(names = {"--no-fix-elevation"}, negatable = true, description = "Fix elevation")
    protected boolean fixElevation = true;

    @Option(names = {"--no-second-precision"}, negatable = true, description = "Create a point per second. Never applied for GPX with data")
    protected boolean pointPerSecond = true;

    @Option(names = {"--no-simulate"}, negatable = true, description = "Simulate a cyclist")
    protected boolean simulate = true;

    @Option(names = {"--gpx-power"}, negatable = true, description = "Reuse GPX power data")
    protected boolean gpxPower = false;

    @Option(names = {"--guess-cx"}, negatable = true, description = "Guess Cx")
    protected boolean guessCx = false;

    @Option(names = {"--guess-weight"}, negatable = true, description = "Guess Cx")
    protected boolean guessWeight = false;

    @Option(names = {"--simulate-speed"}, description = "Cyclist speed (km/h)\nDisable the computation using power, wind, etc")
    protected Double simpleVirtualSpeed = null;

    @CommandLine.Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    protected double powerW = 240;

    @CommandLine.Option(names = {"--cyclist-cx"}, description = "Cyclist Cx")
    protected double cx = 0.3;

    // m.s-2
    @Option(names = {"--wind-speed"}, description = "Wind speed (km/s)")
    protected double windSpeedKmH = 0.0;

    @Option(names = {"--wind-direction"}, description = "Wind direction (°, clockwise, 0=N)")
    protected double windDirectionDegree = 0.0;

    protected Wind wind;

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
        filesMixin.initFiles();
        cyclistMixin.initCyclist();
        init();

        for (File gpxFile : filesMixin.getGpxFiles()) {
            process(gpxFile);
        }
    }

    protected void init() {
        starts = new Instant[1];
        starts[0] = ZonedDateTime.now().minusYears(1).toInstant();

//        System.setProperty("gpx.data.cache", cacheValue);
        wind = new Wind(windSpeedKmH / 3.6, Math.toRadians(windDirectionDegree));
    }

    protected void process(File gpxFile) throws Exception {

        log.info("Processing file {}", gpxFile.getName());
        List<GPXPath> paths = gpxParser.parsePaths(gpxFile);
        File gpxFolder = new File(output, gpxFile.getName()
                .replace(".gpx", ""));
        gpxFolder.mkdirs();
        for (GPXPath path : paths) {
            log.info("Processing path {}", path.getName());

            File pathFolder = new File(gpxFolder, path.getName());
            pathFolder.mkdirs();

            if (!gpxData) {
                GPXFilter.filterPointsDouglasPeucker(path);
            }

            if (fixElevation) {
                gpxElevationFixer.fixElevation(path, !gpxData);
                log.info("D+ : {} m", path.getTotalElevation());

                if (!gpxData) {
                    GPXFilter.filterPointsDouglasPeucker(path);
                }
            }

            if (simulate) {
                Instant start = getNextStart();
                if (simpleVirtualSpeed != null) {
                    simpleTimeComputer.computeTime(path, start, simpleVirtualSpeed / 3.6);
                } else {
                    PowerProvider powerProvider;
                    CxProvider cxProvider = new CxProviderConstant(cx);
                    WindProviderConstant windProvider = new WindProviderConstant(wind);
                    if (gpxPower) {
                        smoothService.smoothPower(path);
                        powerProvider = new PowerProviderFromData();
                        cxProvider = guess(path, cxProvider, windProvider);
                    } else {
                        powerProvider = new PowerProviderConstant(powerW);
                    }
                    Course course = new Course(path, start,
                            cyclistMixin.getCyclist(),
                            powerProvider,
                            windProvider,
                            cxProvider);
                    maxSpeedComputer.computeMaxSpeeds(course);
                    powerComputer.computeTrack(course);
                }
                path.computeArrays(ValueKind.computed);
            }

            if (pointPerSecond && !gpxData) {
                gpxPerSecond.computeOnePointPerSecond(path);
                GPXFilter.filterPointsDouglasPeucker(path);
            }

            log.info("Writing GPX for {}", path.getName());
            gpxFileWriter.writeGpxFile(Collections.singletonList(path), new File(pathFolder, path.getName() + ".gpx"), true);

            if (csv) {
                log.info("Writing CSV for path {}", path.getName());
                csvFileWriter.writeCsvFile(path, new File(pathFolder, path.getName() + ".csv"));
            }

            if (xlsx) {
                log.info("Writing XLSX for path {}", path.getName());
                xlsxFileWriter.writeXlsxFile(path, new File(pathFolder, path.getName() + ".xlsx"));
            }

            log.info("Processed path {}", path.getName());
        }
        gpxFileWriter.writeGpxFile(paths, new File(gpxFolder, gpxFile.getName()));
        log.info("Processed file {}", gpxFile.getName());
    }

    protected CxProvider guess(GPXPath path, CxProvider cxProvider, WindProviderConstant windProvider) {
        if (guessWeight || guessCx) {
            smoothService.smoothSpeed(path);
        }

        if (guessWeight) {
            weightGuesser.guess(path, cyclistMixin.getCyclist(), windProvider, cxProvider);
        }
        if (guessCx) {
            cxGuesser.guess(path, cyclistMixin.getCyclist(),
                    windProvider);
            smoothService.smoothCx(path);
            cxProvider = new CxProviderFromData();
        }

        if (guessWeight || guessCx) {
            path.computeArrays(ValueKind.computed);
        }
        return cxProvider;
    }

}
