package io.github.glandais.fit;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.garmin.fit.CourseMesg;
import com.garmin.fit.DateTime;
import com.garmin.fit.Event;
import com.garmin.fit.EventMesg;
import com.garmin.fit.EventType;
import com.garmin.fit.Field;
import com.garmin.fit.FileEncoder;
import com.garmin.fit.FileIdMesg;
import com.garmin.fit.Fit;
import com.garmin.fit.LapMesg;
import com.garmin.fit.Manufacturer;
import com.garmin.fit.Profile;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.Sport;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;

@Service
public class FitFileWriter {

	private static Field BOUND_MAX_LAT;
	private static Field BOUND_MAX_LON;
	private static Field BOUND_MIN_LAT;
	private static Field BOUND_MIN_LON;

	static {
		try {
			Constructor c = Field.class.getDeclaredConstructor(String.class, int.class, int.class, double.class,
					double.class, String.class, boolean.class, Profile.Type.class);
			c.setAccessible(true);
			BOUND_MAX_LAT = (Field) c.newInstance("bound_max_position_lat", 27, 133, 1.0D, 0.0D, "semicircles", false,
					Profile.Type.SINT32);
			BOUND_MAX_LON = (Field) c.newInstance("bound_max_position_long", 28, 133, 1.0D, 0.0D, "semicircles", false,
					Profile.Type.SINT32);
			BOUND_MIN_LAT = (Field) c.newInstance("bound_min_position_lat", 29, 133, 1.0D, 0.0D, "semicircles", false,
					Profile.Type.SINT32);
			BOUND_MIN_LON = (Field) c.newInstance("bound_min_position_long", 30, 133, 1.0D, 0.0D, "semicircles", false,
					Profile.Type.SINT32);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void writeFitFile(GPXPath path, File target) {
		FileEncoder encode = new FileEncoder(target, Fit.ProtocolVersion.V2_0);

		writeFileId(encode, path);
		writeCourse(encode, path);
		writeLap(encode, path);

		List<Point> points = path.getPoints();
		Point firstWayPoint = points.get(0);
		Date startDate = new Date(firstWayPoint.getTime());
		Point lastWayPoint = points.get(points.size() - 1);
		Date endDate = new Date(lastWayPoint.getTime());

		EventMesg eventMesg = new EventMesg();
		eventMesg.setLocalNum(0);
		eventMesg.setEvent(Event.TIMER);
		eventMesg.setEventType(EventType.START);
		eventMesg.setEventGroup((short) 0);
		eventMesg.setTimestamp(new DateTime(startDate));
		encode.write(eventMesg);

		RecordMesg r = new RecordMesg();
		r.setLocalNum(0);
		for (Point point : points) {
			r.setPositionLat(point.getLatSemi());
			r.setPositionLong(point.getLonSemi());
			r.setDistance((float) (1000.0 * point.getDist()));
			r.setTimestamp(new DateTime(new Date(point.getTime())));
			r.setAltitude((float) point.getZ());
			Double v = point.getData().get("v");
			if (v != null) {
				r.setSpeed((float) (v / 3.6));
			}
			encode.write(r);
		}

		eventMesg.setEvent(Event.TIMER);
		eventMesg.setEventType(EventType.STOP_DISABLE_ALL);
		eventMesg.setEventGroup((short) 0);
		eventMesg.setTimestamp(new DateTime(endDate));
		encode.write(eventMesg);

		encode.close();
	}

	private void writeLap(FileEncoder encode, GPXPath path) {
		List<Point> points = path.getPoints();
		Point firstWayPoint = points.get(0);
		Date startDate = new Date(firstWayPoint.getTime());
		Point lastWayPoint = points.get(points.size() - 1);
		Date endDate = new Date(lastWayPoint.getTime());

		float duration = (float) ((endDate.getTime() - startDate.getTime()) / 1000.0);
		float totaldist = (float) (path.getDist() * 1000.0);

		LapMesg lapMesg = new LapMesg();
		lapMesg.setLocalNum(0);
		lapMesg.setTimestamp(new DateTime(startDate));
		lapMesg.setStartTime(new DateTime(startDate));

		lapMesg.setStartPositionLat(firstWayPoint.getLatSemi());
		lapMesg.setStartPositionLong(firstWayPoint.getLonSemi());

		lapMesg.setEndPositionLat(lastWayPoint.getLatSemi());
		lapMesg.setEndPositionLong(lastWayPoint.getLonSemi());

		lapMesg.setTotalTimerTime(duration);
		lapMesg.setTotalDistance(totaldist);
		lapMesg.setAvgSpeed(totaldist / duration);

		lapMesg.setTotalElapsedTime(duration);

		lapMesg.setTotalAscent((int) path.getTotalElevation());
		lapMesg.setTotalDescent((int) -path.getTotalElevationNegative());

		double maxSpeed = 0.0;
		for (Point point : points) {
			Double v = point.getData().get("v");
			if (v != null && v > maxSpeed) {
				maxSpeed = v;
			}
		}
		lapMesg.setMaxSpeed((float) (maxSpeed / 3.6));

		lapMesg.setMinAltitude((float) path.getMinElevation());
		lapMesg.setMaxAltitude((float) path.getMaxElevation());

		lapMesg.addField(BOUND_MAX_LAT);
		lapMesg.addField(BOUND_MAX_LON);
		lapMesg.addField(BOUND_MIN_LAT);
		lapMesg.addField(BOUND_MIN_LON);

		lapMesg.setFieldValue(27, 0, (Integer) Point.toSemiCircles(path.getMaxlat()), '\uffff');
		lapMesg.setFieldValue(28, 0, (Integer) Point.toSemiCircles(path.getMaxlon()), '\uffff');
		lapMesg.setFieldValue(29, 0, (Integer) Point.toSemiCircles(path.getMinlat()), '\uffff');
		lapMesg.setFieldValue(30, 0, (Integer) Point.toSemiCircles(path.getMinlon()), '\uffff');
		encode.write(lapMesg);
	}

	private void writeCourse(FileEncoder encode, GPXPath path) {
		CourseMesg courseMesg = new CourseMesg();
		courseMesg.setLocalNum(0);
		courseMesg.setName(path.getName());
		courseMesg.setSport(Sport.CYCLING);
		encode.write(courseMesg);
	}

	private void writeFileId(FileEncoder encode, GPXPath path) {
		FileIdMesg fileIdMesg = new FileIdMesg();
		fileIdMesg.setManufacturer(Manufacturer.DYNASTREAM);
		fileIdMesg.setType(com.garmin.fit.File.COURSE);
		fileIdMesg.setProduct(12345);
		fileIdMesg.setSerialNumber(12345L);
		fileIdMesg.setNumber(path.hashCode());
		fileIdMesg.setTimeCreated(new DateTime(new Date()));
		encode.write(fileIdMesg);
	}

}
