package io.github.glandais;

import java.io.File;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.guesser.ConstantsGuesser;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.util.GradeService;
import io.github.glandais.util.SpeedService;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.CourseWithPower;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;

@SpringBootApplication
public class CyclistGuesser implements CommandLineRunner {

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
		SpringApplication.run(CyclistGuesser.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(new File(args[0]));
		GPXPath original = paths.get(0);

		gpxElevationFixer.smoothZ(original, 0.1);
		gradeService.computeGrade(original, "grade");
		speedService.computeSpeed(original, "originalSpeed");
		Cyclist cyclist = new Cyclist(67.2, 0.0, 0.2372, 0.0042, 15, 90, 0.3);

//		Cyclist cyclist = constantsGuesser.guessWithPathWithPower(original);

		Course course = new CourseWithPower(original, cyclist, ZonedDateTime.now());
		maxSpeedComputer.computeMaxSpeeds(course);
		powerComputer.computeTrack(course);
		speedService.computeSpeed(original, "simulatedSpeed");
		for (Point p : original.getPoints()) {
			double dv = p.getData().get("simulatedSpeed") - p.getData().get("originalSpeed");
			p.getData().put("speedDifference", dv);
		}
		gpxFileWriter.writeGpxFile(List.of(original), new File("output", "sim.gpx"));
		gpxFileWriter.writeGpxFile(List.of(original), new File("output", "simAll.gpx"), true);
		gpxFileWriter.writeCsvFile(original, new File("output", "sim.csv"));
	}

}
