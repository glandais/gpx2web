package org.glandais.gpx.elevation.fixer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.glandais.gpx.srtm.Point;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Writes a GPX file.
 * 
 * @author Nicolas Guillaumin
 * 
 */
public class GPXFileWriter {

	/**
	 * XML header.
	 */
	private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>";

	/**
	 * GPX opening tag
	 */
	private static final String TAG_GPX = "<gpx xmlns=\"http://www.topografix.com/GPX/1/1\" "
			+ "creator=\"MapSource 6.16.2\" version=\"1.1\" "
			+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
			+ "xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" "
			+ "xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd\">";

	/**
	 * Date format for a point timestamp.
	 */
	private static final DateTimeFormatter POINT_DATE_FORMATTER = ISODateTimeFormat.dateHourMinuteSecondFraction();
	private static final DecimalFormat df = new DecimalFormat("0.00#########################",
			new DecimalFormatSymbols(Locale.ENGLISH));

	protected GPXFileWriter() {
		super();
	}

	/**
	 * Writes the GPX file
	 * 
	 * @param trackName    Name of the GPX track (metadata)
	 * @param cTrackPoints Cursor to track points.
	 * @param cWayPoints   Cursor to way points.
	 * @param target       Target GPX file
	 * @throws IOException
	 */
	public static void writeGpxFile(List<GPXPath> cTrackPoints, File target) throws IOException {
		if (!target.getParentFile().exists()) {
			target.getParentFile().mkdirs();
		}
		FileWriter fw = new FileWriter(target);

		fw.write(XML_HEADER + "\n");
		fw.write(TAG_GPX + "\n");

		for (GPXPath gpxPath : cTrackPoints) {
			writeTrackPoints(fw, gpxPath);
		}

		fw.write("</gpx>");

		fw.close();
	}

	/**
	 * Iterates on track points and write them.
	 * 
	 * @param trackName Name of the track (metadata).
	 * @param fw        Writer to the target file.
	 * @param c         Cursor to track points.
	 * @throws IOException
	 */
	public static void writeTrackPoints(FileWriter fw, GPXPath gpxPath) throws IOException {
		fw.write("\t" + "<trk>");
		fw.write("\t\t" + "<name>" + gpxPath.getName() + "</name>" + "\n");

		fw.write("\t\t" + "<trkseg>" + "\n");

		List<Point> points = gpxPath.getPoints();
		for (Point point : points) {
			StringBuffer out = new StringBuffer();
			out.append("\t\t\t" + "<trkpt lat=\"" + df.format(point.getLat()) + "\" " + "lon=\""
					+ df.format(point.getLon()) + "\">");
			out.append("<ele>" + df.format(point.getZ()) + "</ele>");
			out.append("<time>" + POINT_DATE_FORMATTER.print(point.getTime()) + "</time>");
			// out.append("<extensions><gpxx:Depth>8</gpxx:Depth></extensions>");
			out.append("</trkpt>" + "\n");
			fw.write(out.toString());
		}

		fw.write("\t\t" + "</trkseg>" + "\n");
		fw.write("\t" + "</trk>" + "\n");
	}

}