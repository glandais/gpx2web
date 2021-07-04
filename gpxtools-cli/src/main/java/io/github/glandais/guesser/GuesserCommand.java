package io.github.glandais.guesser;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.PointField;
import io.github.glandais.gpx.storage.Unit;
import io.github.glandais.gpx.storage.ValueKind;
import io.github.glandais.io.CSVFileWriter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@CommandLine.Command(name = "guesser", mixinStandardHelpOptions = true)
public class GuesserCommand implements Runnable {

    @Autowired
    protected GPXParser gpxParser;

    @Autowired
    protected ConstantsGuesser constantsGuesser;

    @Autowired
    protected PowerComputer powerComputer;

    @Autowired
    protected GPXFileWriter gpxFileWriter;

    @Autowired
    protected GPXElevationFixer gpxElevationFixer;

    @Autowired
    protected MaxSpeedComputer maxSpeedComputer;

    @Autowired
    protected CSVFileWriter csvFileWriter;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @Override
    public void run() {
        cyclistMixin.initCyclist();

        filesMixin.processFiles(gpxParser, this::guess);
    }


    @SneakyThrows
    protected void guess(GPXPath original, File pathFolder) {

        Course course = constantsGuesser.guessWithPathWithPower(original, cyclistMixin.getCyclist());

        log.info("Guessed course : {}", course);

        maxSpeedComputer.computeMaxSpeeds(course);
        powerComputer.computeTrack(course);
//            speedService.computeSpeed(original, PointField.simulatedSpeed, ValueKind.computed);
        for (Point p : original.getPoints()) {
            double dv = p.getCurrent(PointField.simulatedSpeed, Unit.SPEED_S_M) - p.getCurrent(PointField.simulatedSpeed, Unit.SPEED_S_M);
            p.put(PointField.speedDifference, dv, Unit.SPEED_S_M, ValueKind.computed);
        }
        gpxFileWriter.writeGpxFile(List.of(original), new File(pathFolder, "sim.gpx"));
        gpxFileWriter.writeGpxFile(List.of(original), new File(pathFolder, "simAll.gpx"), true);
        csvFileWriter.writeCsvFile(original, new File(pathFolder, "sim.csv"));
    }

}
