package io.github.glandais.process;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.io.CSVFileWriter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
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
import io.github.glandais.virtual.aero.wind.WindProvider;
import io.github.glandais.virtual.aero.wind.WindProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderConstant;
import io.github.glandais.virtual.cyclist.PowerProviderFromData;
import io.github.glandais.virtual.grav.WeightGuesser;
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
@Command(name = "process", mixinStandardHelpOptions = true)
public class ProcessCommand implements Runnable {

    @Inject
    protected GPXParser gpxParser;

    @Inject
    protected GPXElevationFixer gpxElevationFixer;

    @Inject
    protected MaxSpeedComputer maxSpeedComputer;

    @Inject
    protected PowerComputer powerComputer;

    @Inject
    protected GPXFileWriter gpxFileWriter;

    @Inject
    protected SmoothService smoothService;

    @Inject
    protected CSVFileWriter csvFileWriter;

//    @Inject
//    protected XLSXFileWriter xlsxFileWriter;

    @Inject
    protected CxGuesser cxGuesser;

    @Inject
    protected WeightGuesser weightGuesser;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @Option(names = {"--csv"}, negatable = true, description = "Output CSV file")
    protected boolean csv = false;

//    @Option(names = {"--xlsx"}, negatable = true, description = "Output XLSX file")
//    protected boolean xlsx = false;

    @Option(names = {"--gpx-elevation"}, negatable = true, description = "Gpx has valid elevation")
    protected boolean gpxElevation = false;

    @Option(names = {"--gpx-power"}, negatable = true, description = "Gpx has valid power data")
    protected boolean gpxPower = false;

    @CommandLine.Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    protected double powerW = 240;

    @Option(names = {"--guess-cx"}, negatable = true, description = "Guess Cx")
    protected boolean guessCx = false;

    @Option(names = {"--guess-weight"}, negatable = true, description = "Guess Cx")
    protected boolean guessWeight = false;

    @CommandLine.Option(names = {"--cyclist-cx"}, description = "Cyclist Cx")
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
        starts[0] = ZonedDateTime.now().minusYears(1).toInstant();

//        System.setProperty("gpx.data.cache", cacheValue);
        windProvider = new WindProviderConstant(new Wind(windSpeedKmH / 3.6, Math.toRadians(windDirectionDegree)));
        cxProvider = new CxProviderConstant(cx);

        if (gpxPower) {
            powerProvider = new PowerProviderFromData();
        } else {
            powerProvider = new PowerProviderConstant(powerW);
        }
    }

    @SneakyThrows
    private void process(GPXPath path, File pathFolder) {

        if (!gpxElevation) {
            gpxElevationFixer.fixElevation(path, false);
            log.info("D+ : {} m", path.getTotalElevation());
        }

        if (gpxPower) {
            smoothService.smoothPower(path);
            guess(path);
        }

        Instant start = getNextStart();
        Course course = new Course(path, start,
                cyclistMixin.getCyclist(),
                powerProvider,
                windProvider,
                cxProvider);
        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);

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


    protected CxProvider guess(GPXPath path) {
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
