package io.github.glandais;

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
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;

@RestController
public class MapController {

	private final GPXParser gpxParser;

	private final TileMapProducer tileMapProducer;

	public MapController(final GPXParser gpxParser, final TileMapProducer tileMapProducer) {

		this.gpxParser = gpxParser;
		this.tileMapProducer = tileMapProducer;
	}

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/map")
	public void handleFileUpload(@RequestParam("file") MultipartFile file, @RequestParam("tileUrl") String tileUrl,
			@RequestParam("width") Integer width, @RequestParam("height") Integer height, HttpServletResponse response)
			throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			TileMapImage tileMap = tileMapProducer.createTileMap(paths.get(0), tileUrl, 0, width, height);
			response.setContentType("image/png");
			ImageIO.write(tileMap.getImage(), "png", response.getOutputStream());
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

}
