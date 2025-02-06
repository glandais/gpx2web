package io.github.glandais.guesser;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.PointField;
import io.github.glandais.gpx.data.values.Unit;
import io.github.glandais.gpx.data.values.ValueKind;
import io.github.glandais.io.write.tabular.CSVFileWriter;
import io.github.glandais.io.write.GPXFileWriter;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import jakarta.inject.Inject;
import java.io.File;
import java.util.Collections;
import java.util.List;

@Slf4j
@CommandLine.Command(name = "guesser", mixinStandardHelpOptions = true)
public class GuesserCommand implements Runnable {

    @Inject
    protected GPXFileReader gpxFileReader;

    @Inject
    protected ConstantsGuesser constantsGuesser;

    @Inject
    protected PowerComputer powerComputer;

    @Inject
    protected GPXFileWriter gpxFileWriter;

    @Inject
    protected MaxSpeedComputer maxSpeedComputer;

    @Inject
    protected CSVFileWriter csvFileWriter;

    @CommandLine.Mixin
    protected FilesMixin filesMixin;

    @CommandLine.Mixin
    protected CyclistMixin cyclistMixin;

    @Override
    public void run() {
        cyclistMixin.initCyclist();

        filesMixin.processFiles(gpxFileReader, this::guess);
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
        GPX gpx = new GPX(original.getName(), Collections.singletonList(original), List.of());
        gpxFileWriter.writeGpxFile(gpx, new File(pathFolder, "sim.gpx"));
        gpxFileWriter.writeGpxFile(gpx, new File(pathFolder, "simAll.gpx"), true);
        csvFileWriter.writeCsvFile(original, new File(pathFolder, "sim.csv"));
    }

}
