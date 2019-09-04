package io.github.glandais.app;

import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import io.github.glandais.map.MapProducer;

@RestController
public class GPXController {

	private File cache = new File("/tmp");
	
	@Autowired
	private GPXParser gpxParser;

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/")
	public void handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("tileUrl") String tileUrl,
			@RequestParam("tileZoom") Integer tileZoom, HttpServletResponse response) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			MapProducer imageProducer = new MapProducer(cache, tileUrl, paths.get(0), 0.2, tileZoom);
			imageProducer.compute();

			response.setContentType("image/png");
			response.setHeader("Content-Disposition", "filename=export.png");
			ImageIO.write(imageProducer.getImage(), "png", response.getOutputStream());
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

}