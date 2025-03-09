package io.github.glandais.process;

import io.github.glandais.BikeMixin;
import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.tabular.CSVFileWriter;
import io.github.glandais.gpx.io.write.tabular.XLSXFileWriter;
import io.github.glandais.gpx.srtm.GPXElevationFixer;
import io.github.glandais.gpx.util.SmoothService;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.VirtualizeService;
import io.github.glandais.gpx.virtual.maxspeed.MaxSpeedComputer;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroGuesser;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProvider;
import io.github.glandais.gpx.virtual.power.aero.aero.AeroProviderConstant;
import io.github.glandais.gpx.virtual.power.aero.wind.Wind;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProvider;
import io.github.glandais.gpx.virtual.power.aero.wind.WindProviderConstant;
import io.github.glandais.gpx.virtual.power.cyclist.CyclistPowerProvider;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderConstant;
import io.github.glandais.gpx.virtual.power.cyclist.PowerProviderFromData;
import io.github.glandais.gpx.virtual.power.grav.WeightGuesser;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.time.Instant;
import java.time.ZonedDateTime;

@Slf4j
@Command(name = "process", mixinStandardHelpOptions = true)
public class ProcessCommand implements Runnable {

    @Inject
    protected GPXFileReader gpxFileReader;

    @Inject
    protected GPXElevationFixer gpxElevationFixer;

    @Inject
    protected MaxSpeedComputer maxSpeedComputer;

    @Inject
    protected VirtualizeService virtualizeService;

    @Inject
    protected GPXFileWriter gpxFileWriter;

    @Inject
    protected SmoothService smoothService;

    @Inject
    protected CSVFileWriter csvFileWriter;

    @Inject
    protected XLSXFileWriter xlsxFileWriter;

    @Inject
    protected AeroGuesser aeroGuesser;

    @Inject
    protected WeightGuesser weightGuesser;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @CommandLine.Mixin
    protected BikeMixin bikeMixin;

    @Option(names = {"--csv"}, negatable = true, description = "Output CSV file")
    protected boolean csv = false;

    @Option(names = {"--xlsx"}, negatable = true, description = "Output XLSX file")
    protected boolean xlsx = false;

    @Option(names = {"--gpx-elevation"}, negatable = true, description = "Gpx has valid elevation")
    protected boolean gpxElevation = false;

    @Option(names = {"--gpx-power"}, negatable = true, description = "Gpx has valid power data")
    protected boolean gpxPower = false;

    @CommandLine.Option(names = {"--cyclist-power"}, description = "Cyclist power (W)")
    protected double powerW = 240;

//    @Option(names = {"--guess-cx"}, negatable = true, description = "Guess Cx")
//    protected boolean guessCx = false;
//
//    @Option(names = {"--guess-weight"}, negatable = true, description = "Guess Cx")
//    protected boolean guessWeight = false;

    // m.s-2
    @Option(names = {"--wind-speed"}, description = "Wind speed (km/s)")
    protected double windSpeedKmH = 0.0;

    @Option(names = {"--wind-direction"}, description = "Wind direction (°, clockwise, 0=N)")
    protected double windDirectionDegree = 0.0;

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
        starts[0] = ZonedDateTime.now().minusYears(1).toInstant();

//        System.setProperty("gpx.data.cache", cacheValue);
        windProvider = new WindProviderConstant(new Wind(windSpeedKmH / 3.6, Math.toRadians(windDirectionDegree)));
        aeroProvider = new AeroProviderConstant();

        if (gpxPower) {
            powerProvider = new PowerProviderFromData();
        } else {
            powerProvider = new PowerProviderConstant();
        }
    }

    @SneakyThrows
    private void process(GPXPath path, File pathFolder) {

        if (!gpxElevation) {
            gpxElevationFixer.fixElevation(path);
            log.info("D+ : {} m", path.getTotalElevation());
        }

        if (gpxPower) {
            smoothService.smoothPower(path);
            guess(path);
        }

        Instant start = getNextStart();
        Course course = new Course(path, start,
                cyclistMixin.getCyclist(),
                bikeMixin.getBike(),
                powerProvider,
                windProvider,
                aeroProvider);
        maxSpeedComputer.computeMaxSpeeds(course);
        virtualizeService.virtualizeTrack(course);

        log.info("Writing GPX for {}", path.getName());
        gpxFileWriter.writeGPXPath(path, new File(pathFolder, path.getName() + ".gpx"), true);

        if (csv) {
            log.info("Writing CSV for path {}", path.getName());
            csvFileWriter.writeGPXPath(path, new File(pathFolder, path.getName() + ".csv"));
        }

        if (xlsx) {
            log.info("Writing XLSX for path {}", path.getName());
            xlsxFileWriter.writeGPXPath(path, new File(pathFolder, path.getName() + ".xlsx"));
        }

    }


    protected AeroProvider guess(GPXPath path) {
//        if (guessWeight || guessCx) {
//            smoothService.smoothSpeed(path);
//        }
//
//        if (guessWeight) {
//            weightGuesser.guess(path, cyclistMixin.getCyclist(), windProvider, aeroProvider);
//        }
//        if (guessCx) {
//            cxGuesser.guess(path, cyclistMixin.getCyclist(),
//                    windProvider);
//            smoothService.smoothAeroCoef(path);
//            aeroProvider = new AeroProviderFromData();
//        }
//
//        if (guessWeight || guessCx) {
//            path.computeArrays(ValueKind.computed);
//        }
        return aeroProvider;
    }

}
