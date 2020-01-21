package io.github.glandais;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.time.ZonedDateTime;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import io.github.glandais.virtual.Course;
import io.github.glandais.virtual.Cyclist;
import io.github.glandais.virtual.MaxSpeedComputer;
import io.github.glandais.virtual.PowerComputer;

@RestController
public class FitController {

	@Autowired
	private GPXParser gpxParser;

	@Autowired
	private FitFileWriter fitFileWriter;

	@Autowired
	private GPXElevationFixer gpxElevationFixer;

	@Autowired
	private MaxSpeedComputer maxSpeedComputer;

	@Autowired
	private PowerComputer powerComputer;

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/fit")
	public void handleFileUpload(@RequestParam("file") MultipartFile file, HttpServletResponse response)
			throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			gpxElevationFixer.fixElevation(gpxPath);
			double mKg = 80;
			double powerW = 240;
			double maxAngleDeg = 15;
			double maxSpeedKmH = 90;
			double maxBrakeG = 0.3;
			Cyclist cyclist = new Cyclist(mKg, powerW, maxAngleDeg, maxSpeedKmH, maxBrakeG);
			Course course = new Course(gpxPath, cyclist , ZonedDateTime.now());
			maxSpeedComputer.computeMaxSpeeds(course);
			powerComputer.computeTrack(course);

			File tmp = File.createTempFile("fit", "tmp");
			fitFileWriter.writeFitFile(gpxPath, tmp);

			response.setContentType("application/fit");
			try (FileInputStream fis = new FileInputStream(tmp)) {
				IOUtils.copy(fis, response.getOutputStream());
			}
			Files.delete(tmp.toPath());

		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

}