package io.github.glandais.fit;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.garmin.fit.CourseMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.LapMesg;
import com.garmin.fit.Manufacturer;
import com.garmin.fit.RecordMesg;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;

@Service
public class FitFileWriter {

	public void writeFitFile(GPXPath path, File target) {
		FileEncoder encode = new FileEncoder(target, Fit.ProtocolVersion.V2_0);
		List<Point> points = path.getPoints();
		Point start = points.get(0);
		Point finish = points.get(points.size() - 1);

		FileIdMesg fileIdMesg = new FileIdMesg();
		fileIdMesg.setType(com.garmin.fit.File.COURSE);
		fileIdMesg.setManufacturer(Manufacturer.DEVELOPMENT);
		fileIdMesg.setProduct(1);
		fileIdMesg.setSerialNumber(1L);
		fileIdMesg.setTimeCreated(new DateTime(new Date()));
		encode.write(fileIdMesg);

		CourseMesg courseMesg = new CourseMesg();
		courseMesg.setName(path.getName());
		encode.write(courseMesg);

		LapMesg lapMesg = new LapMesg();
		lapMesg.setStartTime(new DateTime(start.getTime()));
		lapMesg.setTimestamp(new DateTime(finish.getTime()));
		lapMesg.setTotalDistance((float) path.getDist());
		encode.write(lapMesg);

		for (Point point : points) {
			RecordMesg recordMesg = new RecordMesg();
			recordMesg.setPositionLat(deg2semicircles(point.getLat()));
			recordMesg.setPositionLong(deg2semicircles(point.getLon()));
			recordMesg.setAltitude((float) point.getZ());
			recordMesg.setDistance((float) point.getDist());
			recordMesg.setTimestamp(new DateTime(point.getTime()));
			encode.write(recordMesg);
		}

		encode.close();
	}

	private Integer deg2semicircles(double deg) {
		return (int) (deg * 0x80000000l / 180.0);
	}

}
