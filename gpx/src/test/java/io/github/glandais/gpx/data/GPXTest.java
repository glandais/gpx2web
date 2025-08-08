package io.github.glandais.gpx.data;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class GPXTest {

    @Test
    void testBasicConstruction() {
        GPXPath path = new GPXPath("Path1", GPXPathType.TRACK);
        GPXWaypoint waypoint = new GPXWaypoint("WP1", new Point());

        GPX gpx = new GPX("Test GPX", Arrays.asList(path), Arrays.asList(waypoint));

        assertEquals("Test GPX", gpx.name());
        assertEquals(1, gpx.paths().size());
        assertEquals(1, gpx.waypoints().size());
    }

    @Test
    void testEmptyGPX() {
        GPX gpx = new GPX("Empty", Collections.emptyList(), Collections.emptyList());

        assertEquals(0.0, gpx.getDist());
        assertEquals(0.0, gpx.getTotalElevation());
        assertEquals(0.0, gpx.getTotalElevationNegative());
        assertEquals(0.0, gpx.getMinlonDeg());
        assertEquals(0.0, gpx.getMaxlonDeg());
        assertEquals(0.0, gpx.getMinlatDeg());
        assertEquals(0.0, gpx.getMaxlatDeg());
    }

    @Test
    void testWithSinglePath() {
        GPXPath path = createTestPath("Path1");
        GPX gpx = new GPX("Single", Collections.singletonList(path), Collections.emptyList());

        // Just verify the methods don't throw exceptions and return reasonable values
        assertTrue(gpx.getDist() >= 0);
        assertTrue(gpx.getTotalElevation() >= 0);
        assertTrue(gpx.getTotalElevationNegative() <= 0);

        // Coordinate methods should return valid numbers
        assertFalse(Double.isNaN(gpx.getMinlonDeg()));
        assertFalse(Double.isNaN(gpx.getMaxlonDeg()));
        assertFalse(Double.isNaN(gpx.getMinlatDeg()));
        assertFalse(Double.isNaN(gpx.getMaxlatDeg()));
    }

    @Test
    void testWithWaypoints() {
        Point wp = new Point();
        wp.setLat(Math.toRadians(45.0));
        wp.setLon(Math.toRadians(90.0));
        GPXWaypoint waypoint = new GPXWaypoint("WP1", wp);

        GPX gpx = new GPX("WithWaypoints", Collections.emptyList(), Collections.singletonList(waypoint));

        // Should include waypoint coordinates in bounds
        assertFalse(Double.isNaN(gpx.getMinlonDeg()));
        assertFalse(Double.isNaN(gpx.getMaxlonDeg()));
        assertFalse(Double.isNaN(gpx.getMinlatDeg()));
        assertFalse(Double.isNaN(gpx.getMaxlatDeg()));
    }

    @Test
    void testRecordMethods() {
        GPXPath path = createTestPath("Path1");
        GPXWaypoint waypoint = new GPXWaypoint("WP1", new Point());

        GPX gpx1 = new GPX("Test", Collections.singletonList(path), Collections.singletonList(waypoint));
        GPX gpx2 = new GPX("Test", Collections.singletonList(path), Collections.singletonList(waypoint));

        // Test equals and hashCode (record methods)
        assertEquals(gpx1, gpx2);
        assertEquals(gpx1.hashCode(), gpx2.hashCode());

        // Test toString
        String str = gpx1.toString();
        assertNotNull(str);
        assertTrue(str.contains("Test"));
    }

    private GPXPath createTestPath(String name) {
        GPXPath path = new GPXPath(name, GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();

        Point p1 = new Point();
        p1.setInstant(baseTime, baseTime);
        p1.setLat(Math.toRadians(45.0));
        p1.setLon(Math.toRadians(90.0));
        p1.setEle(100.0);
        points.add(p1);

        Point p2 = new Point();
        p2.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(10)));
        p2.setLat(Math.toRadians(46.0));
        p2.setLon(Math.toRadians(91.0));
        p2.setEle(110.0);
        points.add(p2);

        path.setPoints(points);
        return path;
    }
}
