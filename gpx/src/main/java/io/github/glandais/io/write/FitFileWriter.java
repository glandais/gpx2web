package io.github.glandais.io.write;

import com.garmin.fit.*;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.data.values.convert.SemiCirclesUnit;
import org.springframework.stereotype.Service;

import jakarta.inject.Singleton;
import java.io.File;
import java.util.Date;
import java.util.List;

@Service
@Singleton
public class FitFileWriter {

    public void writeFitFile(GPXPath path, File target) {
        FileEncoder encode = new FileEncoder(target, Fit.ProtocolVersion.V2_0);

        writeFileId(encode, path);
        writeCourse(encode, path);
        writeLap(encode, path);

        List<Point> points = path.getPoints();
        Point firstWayPoint = points.get(0);
        Date startDate = firstWayPoint.getDate();
        Point lastWayPoint = points.get(points.size() - 1);
        Date endDate = lastWayPoint.getDate();

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
            r.setDistance((float) (point.getDist()));
            r.setTimestamp(new DateTime(point.getDate()));
            r.setAltitude((float) point.getEle());
            encode.write(r);
        }

        eventMesg.setEvent(Event.TIMER);
        eventMesg.setEventType(EventType.STOP_ALL);
        eventMesg.setEventGroup((short) 0);
        eventMesg.setTimestamp(new DateTime(endDate));
        encode.write(eventMesg);

        encode.close();
    }

    private void writeLap(FileEncoder encode, GPXPath path) {
        List<Point> points = path.getPoints();
        Point firstWayPoint = points.get(0);
        Date startDate = firstWayPoint.getDate();
        Point lastWayPoint = points.get(points.size() - 1);
        Date endDate = lastWayPoint.getDate();

        float duration = (float) ((endDate.getTime() - startDate.getTime()) / 1000.0);
        float totaldist = (float) (path.getDist());

        LapMesg lapMesg = new LapMesg();
        lapMesg.setLocalNum(0);
        lapMesg.setTimestamp(new DateTime(endDate));
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
            Double v = point.getSpeed();
            if (v != null && v > maxSpeed) {
                maxSpeed = v;
            }
        }
        lapMesg.setMaxSpeed((float) maxSpeed);

        lapMesg.setMinAltitude((float) path.getMinElevation());
        lapMesg.setMaxAltitude((float) path.getMaxElevation());

        lapMesg.addField(FitFieldBuilder.BOUND_MAX_LAT);
        lapMesg.setFieldValue(27, 0, SemiCirclesUnit.toSemiCircles(path.getMaxlat()), '\uffff');
        lapMesg.addField(FitFieldBuilder.BOUND_MAX_LON);
        lapMesg.setFieldValue(28, 0, SemiCirclesUnit.toSemiCircles(path.getMaxlon()), '\uffff');
        lapMesg.addField(FitFieldBuilder.BOUND_MIN_LAT);
        lapMesg.setFieldValue(29, 0, SemiCirclesUnit.toSemiCircles(path.getMinlat()), '\uffff');
        lapMesg.addField(FitFieldBuilder.BOUND_MIN_LON);
        lapMesg.setFieldValue(30, 0, SemiCirclesUnit.toSemiCircles(path.getMinlon()), '\uffff');

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
