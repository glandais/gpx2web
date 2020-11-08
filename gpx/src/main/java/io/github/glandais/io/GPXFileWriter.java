package io.github.glandais.io;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.storage.unit.StorageUnit;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Writes a GPX file.
 *
 * @author Nicolas Guillaumin
 */
@Service
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

    private static final ThreadLocal<DecimalFormat> LAT_LON_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.00#####", new DecimalFormatSymbols(Locale.ENGLISH)));

    private static final ThreadLocal<DecimalFormat> DIST_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.###", new DecimalFormatSymbols(Locale.ENGLISH)));

    private static final ThreadLocal<DecimalFormat> ELEVATION_FORMATTER = ThreadLocal
            .withInitial(() -> new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH)));

    /**
     * Writes the GPX file
     *
     * @param cTrackPoints Cursor to track points.
     * @param target       Target GPX file
     * @throws IOException
     */
    public void writeGpxFile(List<GPXPath> cTrackPoints, File target) throws IOException {
        writeGpxFile(cTrackPoints, target, false);
    }

    public void writeGpxFile(List<GPXPath> cTrackPoints, File target, boolean extensions) throws IOException {
        FileWriter fw = new FileWriter(target);

        fw.write(XML_HEADER + "\n");
        fw.write(TAG_GPX + "\n");

        for (GPXPath gpxPath : cTrackPoints) {
            writeTrackPoints(fw, gpxPath, extensions);
        }

        fw.write("</gpx>");

        fw.close();
    }

    /**
     * Iterates on track points and write them.
     *
     * @param fw         Writer to the target file.
     * @param extensions
     * @throws IOException
     */
    public void writeTrackPoints(FileWriter fw, GPXPath gpxPath, boolean extensions) throws IOException {
        fw.write("\t" + "<trk>" + "\n");
        fw.write("\t\t" + "<name>" + gpxPath.getName() + "</name>" + "\n");

        fw.write("\t\t" + "<trkseg>" + "\n");

        List<Point> points = gpxPath.getPoints();
        for (Point point : points) {
            StringBuffer out = new StringBuffer();
            out.append("\t\t\t" + "<trkpt lat=\"" + LAT_LON_FORMATTER.get().format(point.getLatDeg()) + "\" " + "lon=\""
                    + LAT_LON_FORMATTER.get().format(point.getLonDeg()) + "\">");
            out.append("<ele>" + ELEVATION_FORMATTER.get().format(point.getZ()) + "</ele>");
            if (point.getTime() != null) {
                out.append("<time>" + DateTimeFormatter.ISO_INSTANT.format(point.getTime()) + "</time>");
            }
            // out.append("<extensions><gpxx:Depth>8</gpxx:Depth></extensions>");
            if (extensions && !point.getData().isEmpty()) {
                out.append("<extensions>");
                point.getData().forEach((k, v) -> {
                    if (v != null) {
                        out.append("<" + k + ">");
                        StorageUnit unit = v.getUnit();
                        out.append(unit.formatData(v.getValue()));
                        out.append("</" + k + ">");
                    }
                });
                out.append("</extensions>");
            }
            out.append("</trkpt>" + "\n");
            fw.write(out.toString());
        }

        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }

}