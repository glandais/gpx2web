package io.github.glandais.guesser;

import io.github.glandais.GpxCommand;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.GradeService;
import io.github.glandais.util.SpeedService;
import io.github.glandais.virtual.*;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Slf4j
@Component
@CommandLine.Command(name = "guesser", mixinStandardHelpOptions = true)
public class GuesserCommand extends GpxCommand {

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

    public static void main(String[] args) {
        SpringApplication.run(GuesserCommand.class, args);
    }

    @Override
    public void run() {

        super.run();

        gpxFiles.stream().forEach(this::guess);
    }

    @SneakyThrows
    private void guess(File file) {

        List<GPXPath> paths = gpxParser.parsePaths(file);
        File gpxFolder = new File(output, file.getName()
                .replace(".gpx", ""));
        gpxFolder.mkdirs();
        for (GPXPath original : paths) {

            log.info("Processing path {}", original.getName());
            File pathFolder = new File(gpxFolder, original.getName());
            pathFolder.mkdirs();

//        Cyclist cyclist = new Cyclist(67.2, 0.0, 15, 90, 0.3, 0.2372, 0.0042);

            Cyclist cyclist = constantsGuesser.guessWithPathWithPower(original);

            log.info("Guessed cyclist : {}", cyclist);

            Course course = new CourseWithPower(original, cyclist, ZonedDateTime.now());
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
