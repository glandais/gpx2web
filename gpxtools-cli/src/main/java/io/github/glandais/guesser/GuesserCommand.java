package io.github.glandais.guesser;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.io.write.tabular.CSVFileWriter;
import io.github.glandais.gpx.virtual.Course;
import io.github.glandais.gpx.virtual.VirtualizeService;
import io.github.glandais.gpx.virtual.maxspeed.MaxSpeedComputer;
import jakarta.inject.Inject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import picocli.CommandLine;

import java.io.File;

@Slf4j
@CommandLine.Command(name = "guesser", mixinStandardHelpOptions = true)
public class GuesserCommand implements Runnable {

    @Inject
    protected GPXFileReader gpxFileReader;

    @Inject
    protected ConstantsGuesser constantsGuesser;

    @Inject
    protected VirtualizeService virtualizeService;

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
        virtualizeService.virtualizeTrack(course);
//            speedService.computeSpeed(original, PointField.simulatedSpeed, ValueKind.computed);
        for (Point p : original.getPoints()) {
//            double dv = p.get(PropertyKeys.simulatedSpeed) - p.get(PropertyKeys.simulatedSpeed);
//            p.put(PropertyKeys.speedDifference, ValueKind.computed, dv);
        }
        gpxFileWriter.writeGPXPath(original, new File(pathFolder, "sim.gpx"));
        gpxFileWriter.writeGPXPath(original, new File(pathFolder, "simAll.gpx"), true);
        csvFileWriter.writeGPXPath(original, new File(pathFolder, "sim.csv"));
    }

}
