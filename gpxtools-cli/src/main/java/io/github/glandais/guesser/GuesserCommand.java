package io.github.glandais.guesser;

import io.github.glandais.CyclistMixin;
import io.github.glandais.FilesMixin;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.GradeService;
import io.github.glandais.util.SpeedService;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.util.List;

@Data
@Slf4j
@Component
@CommandLine.Command(name = "guesser", mixinStandardHelpOptions = true)
public class GuesserCommand implements Runnable {

    @Autowired
    private GPXParser gpxParser;

    @Autowired
    private ConstantsGuesser constantsGuesser;

    @Autowired
    private SpeedService speedService;

    @Autowired
    private PowerComputer powerComputer;

    @Autowired
    private GPXFileWriter gpxFileWriter;

    @Autowired
    private GPXElevationFixer gpxElevationFixer;

    @Autowired
    private GradeService gradeService;

    @Autowired
    private MaxSpeedComputer maxSpeedComputer;

    @Delegate
    @CommandLine.Mixin
    private FilesMixin filesMixin;

    @Delegate
    @CommandLine.Mixin
    private CyclistMixin cyclistMixin;

    @Override
    public void run() {

        initFiles();
        initCyclist();

        filesMixin.getGpxFiles().stream().forEach(this::guess);
    }

    @SneakyThrows
    private void guess(File file) {

        List<GPXPath> paths = gpxParser.parsePaths(file);
        File gpxFolder = new File(filesMixin.getOutput(), file.getName()
                .replace(".gpx", ""));
        gpxFolder.mkdirs();
        for (GPXPath original : paths) {

            log.info("Processing path {}", original.getName());
            File pathFolder = new File(gpxFolder, original.getName());
            pathFolder.mkdirs();

            Course course = constantsGuesser.guessWithPathWithPower(original, getCyclist());

            log.info("Guessed course : {}", course);

            maxSpeedComputer.computeMaxSpeeds(course);
            powerComputer.computeTrack(course);
            speedService.computeSpeed(original, "simulatedSpeed");
            for (Point p : original.getPoints()) {
                double dv = p.getData().get("simulatedSpeed") - p.getData().get("originalSpeed");
                p.getData().put("speedDifference", dv);
            }
            gpxFileWriter.writeGpxFile(List.of(original), new File(pathFolder, "sim.gpx"));
            gpxFileWriter.writeGpxFile(List.of(original), new File(pathFolder, "simAll.gpx"), true);
            gpxFileWriter.writeCsvFile(original, new File(pathFolder, "sim.csv"));
        }
    }

}
