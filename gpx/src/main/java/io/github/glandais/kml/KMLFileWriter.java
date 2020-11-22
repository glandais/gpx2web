package io.github.glandais.kml;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class KMLFileWriter {

    private static final String KML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +

            "<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n" +

            "  <Document>\n" +

            "    <name>%s</name>\n" +

            "    <Style id=\"check-hide-children\">        <!-- define the style for the Document -->\n" +

            "    <ListStyle>\n" +

            "      <listItemType>checkHideChildren</listItemType>\n" +

            "    </ListStyle>\n" +

            "  </Style>\n" +

            "\n" +

            "  <styleUrl>#check-hide-children</styleUrl> <!-- add the style to the Document -->\n" +

            "    <Style id=\"line0\">\n" +

            "      <LineStyle>\n" +

            "        <color>ff0000ff</color>\n" +

            "        <width>6</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n" +

            "    <Style id=\"line1\">\n" +

            "      <LineStyle>\n" +

            "        <color>ff0000dd</color>\n" +

            "        <width>5</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n" +

            "    <Style id=\"line2\">\n" +

            "      <LineStyle>\n" +

            "        <color>ff0000aa</color>\n" +

            "        <width>4</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n" +

            "    <Style id=\"line3\">\n" +

            "      <LineStyle>\n" +

            "        <color>ff000088</color>\n" +

            "        <width>4</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n" +

            "    <Style id=\"line4\">\n" +

            "      <LineStyle>\n" +

            "        <color>ff000066</color>\n" +

            "        <width>4</width>\n" +

            "      </LineStyle>\n" +

            "    </Style>\n" +

            "    <Style id=\"line5\">\n" +

            "      <LineStyle>\n" +

            "        <color>ffff0055</color>\n" +

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

    private static final DecimalFormat df = new DecimalFormat("0.00#########################",
            new DecimalFormatSymbols(Locale.ENGLISH));

    public void writeKmlFile(GPXPath path, File target) throws IOException {
        FileWriter fw = new FileWriter(target);

        fw.write(String.format(KML_HEADER, path.getName()));

        writeTrackPoints(fw, path);

        fw.write(KML_FOOTER);

        fw.close();
    }

    public void writeTrackPoints(FileWriter fw, GPXPath gpxPath) throws IOException {

        List<Point> points = gpxPath.getPoints();
        for (int i = 1; i < points.size(); i++) {
            Point pm1 = points.get(i - 1);
            Point p = points.get(i);

            String coordm1 = df.format(pm1.getLonDeg()) + "," + df.format(pm1.getLatDeg()) + "," + df.format(pm1.getEle());
            String coord = df.format(p.getLonDeg()) + "," + df.format(p.getLatDeg()) + "," + df.format(p.getEle());

            writeTrackPoint(fw, gpxPath, i, p, coordm1, coord, 300000 - 300000, 250000 - 300000, "line0");
            writeTrackPoint(fw, gpxPath, i, p, coordm1, coord, 250000 - 300000, 200000 - 300000, "line1");
            writeTrackPoint(fw, gpxPath, i, p, coordm1, coord, 200000 - 300000, 150000 - 300000, "line2");
            writeTrackPoint(fw, gpxPath, i, p, coordm1, coord, 150000 - 300000, 100000 - 300000, "line3");
            writeTrackPoint(fw, gpxPath, i, p, coordm1, coord, 100000 - 300000, -300000, "line4");
            writeTrackPoint(fw, gpxPath, i, p, coordm1, coord, -300000, -300000, "line5");

        }

    }

    private void writeTrackPoint(FileWriter fw, GPXPath gpxPath, int i, Point p, String coordm1, String coord,
                                 int delta1, int delta2, String style) throws IOException {
        fw.write("    <Placemark>\n");
        fw.write("      <name>" + gpxPath.getName() + i + "-2</name>\n");
        fw.write("      <styleUrl>#" + style + "</styleUrl>\n");
        fw.write("<TimeSpan>\n");
        if (delta1 == delta2) {
            fw.write("<begin>" + print(p.getTime().minusMillis(delta1)) + "</begin>\n");
        } else {
            fw.write("<begin>" + print(p.getTime().minusMillis(delta1)) + "</begin>\n");
            fw.write("<end>" + print(p.getTime().minusMillis(delta2)) + "</end>\n");
        }
        fw.write("</TimeSpan>\n");
        fw.write("      <LineString>\n");
        fw.write("        <extrude>1</extrude>\n");
        fw.write("        <tessellate>1</tessellate>\n");
        fw.write("        <altitudeMode>clampToGround</altitudeMode>\n");
        fw.write("        <coordinates>\n");
        fw.write(coordm1 + "\n");
        fw.write(coord + "\n");
        fw.write("        </coordinates>\n");
        fw.write("      </LineString>\n");
        fw.write("    </Placemark>\n");
    }

    private String print(Instant l) {
        return l.toString();
    }

}
