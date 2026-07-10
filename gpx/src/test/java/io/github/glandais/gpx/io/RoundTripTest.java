package io.github.glandais.gpx.io;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.GPXPathType;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import java.io.File;
import java.time.Instant;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;

class RoundTripTest {

    /** computeArrays() needs an instant on every point, exactly as the reader sets one. */
    private static Point point(double latDeg, double lonDeg, double ele) {
        Point p = new Point();
        p.setLat(Math.toRadians(latDeg));
        p.setLon(Math.toRadians(lonDeg));
        p.setEle(ele);
        p.setInstant(null, Instant.EPOCH);
        return p;
    }

    private static File write(GPX gpx, String prefix) throws Exception {
        File target = File.createTempFile(prefix, ".gpx");
        target.deleteOnExit();
        new GPXFileWriter().writeGPX(gpx, target);
        return target;
    }

    /**
     * The emoji is the whole point: escape() used to write it as two surrogate references, so a file
     * this library produced could not be read back by this library.
     */
    @Test
    void writtenGpxShouldBeReadableBack() throws Exception {
        GPXPath path = new GPXPath("Sortie 🤘 à Bertaudière", GPXPathType.TRACK);
        path.addPoint(point(45.1, 6.4, 1200.0));
        path.addPoint(point(45.2, 6.5, 1300.0));
        path.computeArrays();
        GPX original = new GPX("Col du Galibier 🤘", List.of(path), List.of());

        GPX reread = new GPXFileReader().parseGPX(write(original, "roundtrip"));

        assertEquals(original.name(), reread.name());
        assertEquals(1, reread.paths().size());
        assertEquals(path.getName(), reread.paths().get(0).getName());
        assertEquals(2, reread.paths().get(0).getPoints().size());
    }

    /**
     * The round trip alone cannot police the writer: the reader repairs surrogate-pair references,
     * so a broken writer still survives it. Parse the output with a stock parser instead, which
     * grants no such mercy, and assert what we actually care about — the file we emit is valid XML.
     */
    @Test
    void writtenGpxShouldBeWellFormedXmlWithoutAnyRepair() throws Exception {
        GPXPath path = new GPXPath("Sortie 🤘", GPXPathType.TRACK);
        path.addPoint(point(45.1, 6.4, 1200.0));
        path.addPoint(point(45.2, 6.5, 1300.0));
        path.computeArrays();
        GPX original = new GPX("Col 🤘", List.of(path), List.of());

        File target = write(original, "roundtrip-wellformed");

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        Document document = dbf.newDocumentBuilder().parse(target);

        assertEquals("gpx", document.getDocumentElement().getTagName());
    }

    /** Elevation is written with a single decimal, so the round trip is lossy by construction. */
    @Test
    void writtenGpxShouldPreserveCoordinatesWithinWriterPrecision() throws Exception {
        GPXPath path = new GPXPath("precision", GPXPathType.TRACK);
        path.addPoint(point(45.1234567, 6.7654321, 1234.56));
        path.addPoint(point(45.2, 6.5, 1300.0));
        path.computeArrays();
        GPX original = new GPX("precision", List.of(path), List.of());

        Point p = new GPXFileReader()
                .parseGPX(write(original, "roundtrip-precision"))
                .paths()
                .get(0)
                .getPoints()
                .get(0);

        assertEquals(45.1234567, p.getLatDeg(), 1e-6);
        assertEquals(6.7654321, p.getLonDeg(), 1e-6);
        assertEquals(1234.56, p.getEle(), 0.05);
    }
}
