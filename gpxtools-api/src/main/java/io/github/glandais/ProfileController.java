package io.github.glandais;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXCharter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;

@RestController
public class ProfileController {

	private final GPXParser gpxParser;

	private final GPXElevationFixer gpxElevationFixer;

	private final GPXCharter gpxCharter;

	public ProfileController(final GPXParser gpxParser, final GPXElevationFixer gpxElevationFixer, final GPXCharter gpxCharter) {

		this.gpxParser = gpxParser;
		this.gpxElevationFixer = gpxElevationFixer;
		this.gpxCharter = gpxCharter;
	}

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/profile")
	public void handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("width") Integer width,
			@RequestParam("height") Integer height, HttpServletResponse response) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			gpxElevationFixer.fixElevation(gpxPath);

			File tmp = File.createTempFile("chart", "tmp");
			gpxCharter.createChartWeb(gpxPath, tmp, width, height);

			response.setContentType("image/png");
			try (FileInputStream fis = new FileInputStream(tmp)) {
				IOUtils.copy(fis, response.getOutputStream());
			}
			Files.delete(tmp.toPath());

		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

}
