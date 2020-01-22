package io.github.glandais;

import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
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
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.utils.PolylineUtils;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import io.github.glandais.map.TileMapProducer;

@RestController
public class MapboxController {

	@Autowired
	private GPXParser gpxParser;

	public static void main(String[] args) {
		String polyline = "qxb_Hb%7BlHg@yJaDj@yAcZ%5Ey@MeHqIeLiFuB%7BDLwM%7CFm@SDeSaDke@uOcs@qEmNuAcBoAqM%7BJib@qAiV_EaQwFc_@kIqZgNem@eKgQkA_@yIiNcHmS%7DM_HuDuCyDoEoM%7DSsH_H%7D%5Cwi@oC_J_FaVyCgKwBqEyG_IcKqI%7BIiJ_O%7BToMaP[qA%7BH%7BIg@cBeJoNgPpXwE%60G%7D@v@QSuFmKqEmOsEwHkAmD%7DIyKmSo%5CkJ%7DWq@_AgGmDyGyI%7DJkEeAoAyN%7BSiOa[eD%7DMyEqZaKeT%7DDyU%3FwDg@Du@%7DAmDmO_@wIyADaA%60Cm@dGsBNoBhAuGt@ImH%7CAoCYqFO_@mE%7BAyCoJeFuEiKeG_Gg@sChAoDEsLmHyH_LOoI_CkToBiKeFuFuAoCt@sJeDyWDcB%60Io_@J%7DBaAmJjBwFJqBYcBuGaKyAaEuEsEPuAp@HhAhBb@aAmB%7DGeEsHwLeN%7BKcD%7BTsQeo@_Su@nCqF%60EoDxAkGj@yQbHaAl@kOhUeGhGs%60@dx@uLxNoMhSgvArbCwS~j@]%60CB%60u@%60BbS%7C@EhC%60AfKEzBl@pOpQdClBx%7D@raApArCvCbOrKbH~AvBft@tdDz@z@dd@rNxArAvTr%5Cj@xAJpHdElFh@rHjAfDfXpX~GfEdIbSrK%60Ph@nESbFdDWfEt@~Z%7CNjD%60@vFeEXRzAxH%60@~FmE~P%3FxA%7DAvByBzJfH%7CJjA%7CC~AfNsBrI~EnSbGzQrBzCFvAp@L%60AuBtHeEhI_HjIiFzAb@x@c@xHpBna@xE~DxCfIzMtMj[IxAbBdCTPp@a@fF%60A%5Ej@LYjHvF%3Fv@j@I%60FrEvDxEd@fB%60CD%60NlDbBhApCuMXIhEbAdCdC%60DhAhFbDtLbVxH~GbGtOpH%60NzInG%60DpDzDhB~EH%60MaC%7CBz@lKvHhB%60@ZWbBp@%60Jm@lLv@dLItH~DvIhJ%7CFtCxIzBlAtIxFvKz@lQdAnEv@rA%60GnE~T~V%60BfX~H%7CU%7CApIdLNzR_%5EnAfB@lC%7CAz@xVsCzFmFjG%60P%60@rCxErJhBrAfEHtNkEnLcLvA%7BF%7BDqM";
		polyline = URLDecoder.decode(polyline, StandardCharsets.UTF_8);
		System.out.println(polyline);
		List<Point> points = PolylineUtils.decode(polyline, 5);
		System.out.println(points);
	}

	@CrossOrigin(origins = "https://gabriel.landais.org")
	@PostMapping("/mapbox")
	public void handleFileUpload(@RequestParam("file") MultipartFile file,
			@RequestParam("accessToken") String accessToken, @RequestParam("maxSize") Integer maxSize,
			HttpServletResponse response) throws Exception {
		List<GPXPath> paths = gpxParser.parsePaths(file.getInputStream());
		if (paths.size() == 1) {
			GPXPath gpxPath = paths.get(0);
			List<io.github.glandais.gpx.Point> points = gpxPath.getPoints();
			io.github.glandais.gpx.Point start = points.get(0);
			io.github.glandais.gpx.Point end = points.get(points.size() - 1);
			StaticMarkerAnnotation pinStart = StaticMarkerAnnotation.builder()
					.iconUrl("https://n-peloton.fr/maps/pin-icon-start.png")
					.lnglat(Point.fromLngLat(start.getLon(), start.getLat())).build();
			StaticMarkerAnnotation pinEnd = StaticMarkerAnnotation.builder()
					.iconUrl("https://n-peloton.fr/maps/pin-icon-end.png")
					.lnglat(Point.fromLngLat(end.getLon(), end.getLat())).build();
			List<StaticMarkerAnnotation> staticMarkerAnnotations = List.of(pinStart, pinEnd);
			List<Point> mPoints = points.stream().map(p -> Point.fromLngLat(p.getLon(), p.getLat()))
					.collect(Collectors.toList());
			mPoints = PolylineUtils.simplify(mPoints, 0.0001);
//			String polyline = PolylineUtils.encode(mPoints, 5);
			String polyline = LineString.fromLngLats(mPoints).toString();
			StaticPolylineAnnotation line = StaticPolylineAnnotation.builder().fillColor("red").fillOpacity(0.6f)
					.strokeWidth(5.0).polyline(polyline).build();
			List<StaticPolylineAnnotation> staticPolylineAnnotations = List.of(line);
			URL url = MapboxStaticMap.builder().accessToken(accessToken).cameraAuto(true).width(maxSize).height(maxSize)
					.staticMarkerAnnotations(staticMarkerAnnotations)
					.staticPolylineAnnotations(staticPolylineAnnotations).build().url().url();
			System.out.println(url);

			IOUtils.copy(url.openStream(), response.getOutputStream());
		} else {
			throw new IllegalArgumentException("0 or more than 1 path found");
		}
	}

}