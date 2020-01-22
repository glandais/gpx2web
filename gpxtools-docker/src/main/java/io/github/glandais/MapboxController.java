package io.github.glandais;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mapbox.api.staticmap.v1.MapboxStaticMap;
import com.mapbox.api.staticmap.v1.models.StaticMarkerAnnotation;
import com.mapbox.api.staticmap.v1.models.StaticPolylineAnnotation;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;

@RestController
public class MapboxController {

	@Autowired
	private GPXParser gpxParser;

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/mapbox")
	public void handleFileUpload(@RequestParam("file") MultipartFile file,
			@RequestParam("accessToken") String accessToken, @RequestParam("maxSize") Integer maxSize,
			HttpServletResponse response) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			List<io.github.glandais.gpx.Point> points = gpxPath.getPoints();
//			io.github.glandais.gpx.Point start = points.get(0);
//			io.github.glandais.gpx.Point end = points.get(points.size() - 1);
//			StaticMarkerAnnotation pinStart = StaticMarkerAnnotation.builder()
//					.iconUrl("https://n-peloton.fr/maps/pin-icon-start.png")
//					.lnglat(Point.fromLngLat(start.getLon(), start.getLat())).build();
//			StaticMarkerAnnotation pinEnd = StaticMarkerAnnotation.builder()
//					.iconUrl("https://n-peloton.fr/maps/pin-icon-end.png")
//					.lnglat(Point.fromLngLat(end.getLon(), end.getLat())).build();
//			List<StaticMarkerAnnotation> staticMarkerAnnotations = List.of(pinStart, pinEnd);
			List<StaticMarkerAnnotation> staticMarkerAnnotations = List.of();
			List<Point> mPoints = points.stream().map(p -> Point.fromLngLat(p.getLon(), p.getLat()))
					.collect(Collectors.toList());
			mPoints = PolylineUtils.simplify(mPoints, 0.0001);
			String polyline = PolylineUtils.encode(mPoints, 5);
			StaticPolylineAnnotation line = StaticPolylineAnnotation.builder().strokeWidth(5.0).strokeColor(255, 0, 0)
					.strokeOpacity(0.6f).polyline(polyline).build();
			List<StaticPolylineAnnotation> staticPolylineAnnotations = List.of(line);
			URL url = MapboxStaticMap.builder().accessToken(accessToken).cameraAuto(true).width(maxSize).height(maxSize)
					.styleId("outdoors-v11").staticMarkerAnnotations(staticMarkerAnnotations)
					.staticPolylineAnnotations(staticPolylineAnnotations).build().url().url();
			System.out.println(url);

			IOUtils.copy(url.openStream(), response.getOutputStream());
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

}