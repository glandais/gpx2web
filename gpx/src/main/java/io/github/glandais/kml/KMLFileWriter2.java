package io.github.glandais.kml;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class KMLFileWriter2 {

    private static final String KML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +

            "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +

            "  <Document>\n" +

            "    <name>Paths</name>\n" +

            "    <Style id=\"check-hide-children\">        <!-- define the style for the Document -->\n" +

            "    <ListStyle>\n" +

            "      <listItemType>checkHideChildren</listItemType>\n" +

            "    </ListStyle>\n" +

            "  </Style>\n" +

            "\n" +

            "  <styleUrl>#check-hide-children</styleUrl> <!-- add the style to the Document -->\n" +

            "    <Style id=\"redLine\">\n" +

            "      <LineStyle>\n" +

            "        <color>7f0000ff</color>\n" +

            "        <width>6</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n" +

            "    <Style id=\"yellowLine\">\n" +

            "      <LineStyle>\n" +

            "        <color>7f0088ff</color>\n" +

            "        <width>4</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n";

    // -112.2550785337791,36.07954952145647,2357
    // -112.2549277039738,36.08117083492122,2357
    // -112.2552505069063,36.08260761307279,2357
    // -112.2564540158376,36.08395660588506,2357
    // -112.2580238976449,36.08511401044813,2357
    // -112.2595218489022,36.08584355239394,2357
    // -112.2608216347552,36.08612634548589,2357
    // -112.262073428656,36.08626019085147,2357
    // -112.2633204928495,36.08621519860091,2357
    // -112.2644963846444,36.08627897945274,2357
    // -112.2656969554589,36.08649599090644,2357
    private static final String KML_FOOTER = "  </Document>\n" +

            "</kml>\n";

    private static final DecimalFormat df = new DecimalFormat("0.00#########################", new DecimalFormatSymbols(Locale.ENGLISH));

    public static void writeKmlFile(List<GPXPath> paths, File target) throws IOException {
        FileWriter fw = new FileWriter(target);

        fw.write(KML_HEADER);

        fw.write("<gx:Tour><name>" + target.getName() + " Tour</name><gx:Playlist>\n");
        for (GPXPath gpxPath : paths) {
            writeTour(fw, gpxPath);
        }
        fw.write("</gx:Playlist></gx:Tour>\n");

        fw.write("<Folder><name>" + target.getName() + " Paths</name>\n");
        for (GPXPath gpxPath : paths) {
            writeStyles(fw, gpxPath);
        }

        for (GPXPath gpxPath : paths) {
            writeTrackPoints(fw, gpxPath);
        }
        fw.write("</Folder>\n");

        fw.write(KML_FOOTER);

        fw.close();
    }

    private static void writeTour(FileWriter fw, GPXPath gpxPath) throws IOException {
        List<Point> points = gpxPath.getPoints();
        for (int i = 1; i < points.size(); i++) {
            Point pm1 = points.get(i - 1);
            Point p = points.get(i);

            double sleep = (1.0 * (p.getTime().toEpochMilli() - pm1.getTime().toEpochMilli())) / 1000.0;

            fw.write(" <gx:AnimatedUpdate>\n");
            fw.write("  <gx:duration>3.0</gx:duration>\n");
            fw.write("  <Update>\n");
            fw.write("   <Change>\n");
            fw.write("    <Placemark targetId=\"" + gpxPath.getName() + "-" + i + "\">\n");
            fw.write("     <styleUrl>#yellowLine</styleUrl>\n");
            fw.write("    </Placemark>\n");
            fw.write("   </Change>\n");
            fw.write("  </Update>\n");
            fw.write(" </gx:AnimatedUpdate>\n");

            fw.write(" <gx:Wait>\n");
            fw.write("  <gx:duration>" + df.format(sleep) + "</gx:duration>\n");
            fw.write(" </gx:Wait>\n");
        }
    }

    public static void writeStyles(FileWriter fw, GPXPath gpxPath) throws IOException {

        //		List<Point> points = gpxPath.getPoints();
        //		for (int i = 1; i < points.size(); i++) {
        //			fw.write("    <Style id=\"style" + gpxPath.getName() + "-" + i + "\">\n");
        //			fw.write("      <LineStyle id=\"redLine" + gpxPath.getName() + "-" + i + "\">\n");
        //			fw.write("        <color>7f0000ff</color>\n");
        //			fw.write("        <width>6</width>\n");
        //			fw.write("      </LineStyle>\n");
        //			fw.write("    </Style>\n");
        //		}
    }

    public static void writeTrackPoints(FileWriter fw, GPXPath gpxPath) throws IOException {

        List<Point> points = gpxPath.getPoints();
        for (int i = 1; i < points.size(); i++) {
            Point pm1 = points.get(i - 1);
            Point p = points.get(i);

            fw.write("    <Placemark id=\"" + gpxPath.getName() + "-" + i + "\">\n");
            fw.write("      <name>" + gpxPath.getName() + "-" + i + "</name>\n");
            //			fw.write("      <styleUrl>#style" + gpxPath.getName() + "-" + i + "</styleUrl>\n");
            fw.write("      <styleUrl>#redLine</styleUrl>\n");
            fw.write("      <LineString>\n");
            fw.write("        <extrude>1</extrude>\n");
            fw.write("        <tessellate>1</tessellate>\n");
            fw.write("        <altitudeMode>clampToGround</altitudeMode>\n");
            fw.write("        <coordinates>\n");
            fw.write(df.format(pm1.getLonDeg()) + "," + df.format(pm1.getLatDeg()) + "," + df.format(pm1.getZ()) + "\n");
            fw.write(df.format(p.getLonDeg()) + "," + df.format(p.getLatDeg()) + "," + df.format(p.getZ()) + "\n");
            fw.write("        </coordinates>\n");
            fw.write("      </LineString>\n");
            fw.write("    </Placemark>\n");
        }

    }

}
