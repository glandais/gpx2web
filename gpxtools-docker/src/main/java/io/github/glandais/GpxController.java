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

import io.github.glandais.gpx.GPXFilter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;

@RestController
public class GpxController {

	@Autowired
	private GPXParser gpxParser;

	@Autowired
	private GPXPathEnhancer gpxPathEnhancer;

	@Autowired
	private GPXFileWriter gpxFileWriter;

	@Autowired
	private GPXFilter gpxFilter;

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/simplify")
	public void simplify(@RequestParam("file") MultipartFile file, HttpServletResponse response) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			gpxPathEnhancer.virtualize(gpxPath);
			gpxFilter.filterPointsDouglasPeucker(gpxPath);

			File tmp = File.createTempFile("gpx", "tmp");
			gpxFileWriter.writeGpxFile(paths, tmp);

			response.setContentType("application/gpx");
			try (FileInputStream fis = new FileInputStream(tmp)) {
				IOUtils.copy(fis, response.getOutputStream());
			}
			Files.delete(tmp.toPath());
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/gpxinfo")
	public String gpxinfo(@RequestParam("file") MultipartFile file) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			gpxPathEnhancer.virtualize(gpxPath);
			gpxFilter.filterPointsDouglasPeucker(gpxPath);
			return Math.round(gpxPath.getDist()) + "km " + Math.round(gpxPath.getTotalElevation()) + " m↑ "
					+ Math.round(gpxPath.getTotalElevationNegative()) + " m↓";
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/gpxinfo2")
	public GPXInfo gpxinfo2(@RequestParam("file") MultipartFile file) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			gpxPathEnhancer.virtualize(gpxPath);
			gpxFilter.filterPointsDouglasPeucker(gpxPath);
			float dist = Math.round(10.0 * gpxPath.getDist()) / 10.0f;
			return new GPXInfo(dist, (int) Math.round(gpxPath.getTotalElevation()),
					(int) Math.round(gpxPath.getTotalElevationNegative()));
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}
}
