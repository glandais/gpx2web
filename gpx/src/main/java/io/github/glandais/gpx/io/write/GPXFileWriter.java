package io.github.glandais.gpx.io.write;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.GPXWaypoint;
import io.github.glandais.gpx.data.Point;
import jakarta.inject.Singleton;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Writes a GPX file.
 *
 * @author Nicolas Guillaumin
 */
@Service
@Singleton
public class GPXFileWriter implements FileExporter {

    /**
     * XML header.
     */
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>";

    /**
     * GPX opening tag
     */
    private static final String TAG_GPX = "<gpx " +
            "creator=\"https://www.mapstogpx.com/strava\" " +
            "version=\"1.1\" " +
            "xmlns=\"http://www.topografix.com/GPX/1/1\" " +
            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xmlns:gpxx=\"http://www.garmin.com/xmlschemas/GpxExtensions/v3\" " +
            "xmlns:gpxtpx=\"http://www.garmin.com/xmlschemas/TrackPointExtension/v1\" " +
            "xmlns:gpxpx=\"http://www.garmin.com/xmlschemas/PowerExtension/v1\" " +
            "xsi:schemaLocation=\"" +
            "http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd " +
            "http://www.garmin.com/xmlschemas/GpxExtensions/v3 https://www8.garmin.com/xmlschemas/GpxExtensionsv3.xsd " +
            "http://www.garmin.com/xmlschemas/TrackPointExtension/v1 http://www8.garmin.com/xmlschemas/TrackPointExtensionv1.xsd " +
            "http://www.garmin.com/xmlschemas/PowerExtension/v1 http://www.garmin.com/xmlschemas/PowerExtensionv1.xsd" +
            "\">";

    private static final ThreadLocal<DecimalFormat> LAT_LON_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.00#####", new DecimalFormatSymbols(Locale.ENGLISH)));

    private static final ThreadLocal<DecimalFormat> ELEVATION_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH)));

    @Override
    public void writeGPXPath(GPXPath path, File file) throws IOException {
        writeGPXPath(path, file, false);
    }

    public void writeGPXPath(GPXPath path, File file, boolean extensions) throws IOException {
        GPX gpx = new GPX(path.getName(), List.of(path), List.of());
        writeGPX(gpx, file, extensions);
    }

    /**
     * Writes the GPX file
     *
     * @param gpx    gpx.
     * @param target Target GPX file
     * @throws IOException
     */
    public void writeGPX(GPX gpx, File target) throws IOException {
        writeGPX(gpx, target, false);
    }

    public void writeGPX(GPX gpx, File target, boolean extensions) throws IOException {
        FileWriter fw = new FileWriter(target);

        fw.write(XML_HEADER + "\n");
        fw.write(TAG_GPX + "\n");

        if (gpx.name() != null) {
            fw.write("\t" + "<metadata>" + "\n");
            fw.write("\t\t" + "<name>" + escape(gpx.name()) + "</name>" + "\n");
            fw.write("\t" + "</metadata>" + "\n");
        }

        for (GPXWaypoint waypoint : gpx.waypoints()) {
            writeWaypoint(fw, waypoint);
        }
        for (GPXPath gpxPath : gpx.paths()) {
            writePath(fw, gpxPath, extensions);
        }

        fw.write("</gpx>");

        fw.close();
    }

    private void writeWaypoint(FileWriter fw, GPXWaypoint waypoint) throws IOException {
        String out = "\t<wpt lat=\"" +
                LAT_LON_FORMATTER.get().format(waypoint.point().getLatDeg()) + "\" " +
                "lon=\"" + LAT_LON_FORMATTER.get().format(waypoint.point().getLonDeg()) + "\">\n" +
                "\t\t" + "<name>" + escape(waypoint.name()) + "</name>" + "\n" +
                "\t</wpt>\n";
        fw.write(out);
    }

    /**
     * Iterates on track points and write them.
     *
     * @param fw         Writer to the target file.
     * @param extensions
     * @throws IOException
     */
    public void writePath(FileWriter fw, GPXPath gpxPath, boolean extensions) throws IOException {
        fw.write("\t" + "<trk>" + "\n");
        fw.write("\t\t" + "<name>" + escape(gpxPath.getName()) + "</name>" + "\n");

        fw.write("\t\t" + "<trkseg>" + "\n");

        List<Point> points = gpxPath.getPoints();
        for (Point point : points) {
            StringBuffer out = new StringBuffer();
            out.append("\t\t\t" + "<trkpt lat=\"").append(LAT_LON_FORMATTER.get().format(point.getLatDeg())).append("\" ")
                    .append("lon=\"").append(LAT_LON_FORMATTER.get().format(point.getLonDeg())).append("\">");
            out.append("<ele>").append(ELEVATION_FORMATTER.get().format(point.getEle())).append("</ele>");
            if (point.getInstant() != null) {
                out.append("<time>").append(DateTimeFormatter.ISO_INSTANT.format(point.getInstant())).append("</time>");
            }
            // out.append("<extensions><gpxx:Depth>8</gpxx:Depth></extensions>");
            Map<String, String> gpxData = point.getGpxData();
            if (extensions && !gpxData.isEmpty()) {
                out.append("<extensions>");
                gpxData.forEach((k, v) -> {
                    if (v != null) {
                        out.append("<").append(k).append(">");
                        out.append(escape(v));
                        out.append("</").append(k).append(">");
                    }
                });
                out.append("</extensions>");
            }
            out.append("</trkpt>\n");
            fw.write(out.toString());
        }

        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }

    public static String escape(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '\'' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}