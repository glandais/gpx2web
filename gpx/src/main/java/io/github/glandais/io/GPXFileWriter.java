package io.github.glandais.io;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                        out.append(String.valueOf(v));
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

    public void writeCsvFile(GPXPath path, File file) throws IOException {
        List<Point> points = path.getPoints();
        Map<String, Function<Point, String>> columns = new LinkedHashMap<>();

        columns.put("lon", p -> LAT_LON_FORMATTER.get().format(p.getLonDeg()));
        columns.put("lat", p -> LAT_LON_FORMATTER.get().format(p.getLatDeg()));
        columns.put("z", p -> ELEVATION_FORMATTER.get().format(p.getZ()));
        columns.put("dist", p -> DIST_FORMATTER.get().format(p.getDist()));
        columns.put("time", p -> DateTimeFormatter.ISO_INSTANT.format(p.getTime()));

        Set<String> attributes = new TreeSet<>();
        for (Point point : points) {
            point.getData().forEach((k, v) -> attributes.add(k));
        }
        for (String attribute : attributes) {
            if (!columns.containsKey(attribute)) {
                columns.put(attribute, p -> {
                    Double value = p.getData().get(attribute);
                    if (value == null) {
                        return "";
                    } else {
                        return DIST_FORMATTER.get().format(value);
                    }
                });
            }
        }

        Map<String, List<String>> collections = new TreeMap<>();
        for (Map.Entry<String, Function<Point, String>> entry : columns.entrySet()) {
            List<String> collection = points.stream().map(entry.getValue()).collect(Collectors.toList());
            // verify is same data already exist
            boolean add = true;
            for (List<String> otherCollection : collections.values()) {
                if (add && collection.equals(otherCollection)) {
                    add = false;
                }
            }
            if (add) {
                collections.put(entry.getKey(), collection);
            }
        }

        FileWriter fw = new FileWriter(file);
        fw.write(collections.keySet().stream().collect(Collectors.joining(";")) + "\n");
        for (int i = 0; i < points.size(); i++) {
            List<String> line = new ArrayList<>(collections.size());
            for (String collection : collections.keySet()) {
                line.add(collections.get(collection).get(i));
            }
            fw.write(line.stream().collect(Collectors.joining(";")) + "\n");
        }
        fw.close();
    }

}