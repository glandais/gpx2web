package io.github.glandais.gpx.data;

import static org.junit.jupiter.api.Assertions.*;

import io.github.glandais.gpx.data.values.PropertyKeys;
import io.github.glandais.gpx.data.values.converter.Converters;
import io.github.glandais.gpx.util.Constants;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PointTest {

    @Test
    void testConstructors() {
        // Test default constructor
        Point p1 = new Point();
        assertNotNull(p1);

        // Test copy constructor
        p1.setLat(45.5);
        p1.setLon(2.3);
        p1.setEle(100.0);

        Point p2 = new Point(p1);
        assertEquals(45.5, p2.getLat());
        assertEquals(2.3, p2.getLon());
        assertEquals(100.0, p2.getEle());

        // Verify it's a deep copy
        p1.setLat(50.0);
        assertEquals(45.5, p2.getLat()); // p2 should not change
    }

    @Test
    void testBasicGettersAndSetters() {
        Point p = new Point();

        // Test lat/lon
        p.setLat(48.8566);
        p.setLon(2.3522);
        assertEquals(48.8566, p.getLat());
        assertEquals(2.3522, p.getLon());

        // Test elevation
        p.setEle(35.0);
        assertEquals(35.0, p.getEle());

        // Test null elevation returns 0
        Point p2 = new Point();
        assertEquals(0.0, p2.getEle());

        // Test grade
        p.setGrade(0.05);
        assertEquals(0.05, p.getGrade());

        // Test power
        p.setPower(250.0);
        assertEquals(250.0, p.getPower());

        // Test speed
        p.setSpeed(8.33);
        assertEquals(8.33, p.getSpeed());

        // Test distance
        p.setDist(1000.0);
        assertEquals(1000.0, p.getDist());

        // Test bearing
        p.setBearing(1.57);
        assertEquals(1.57, p.getBearing());
    }

    @Test
    void testCoordinateConversions() {
        Point p = new Point();
        p.setLat(Math.toRadians(45.0));
        p.setLon(Math.toRadians(90.0));

        // Test degree conversions
        assertEquals(45.0, p.getLatDeg(), 0.001);
        assertEquals(90.0, p.getLonDeg(), 0.001);

        // Test semicircle conversions
        int expectedLatSemi = (int) (45.0 * (1L << 31) / 180.0);
        int expectedLonSemi = (int) (90.0 * (1L << 31) / 180.0);
        assertEquals(expectedLatSemi, p.getLatSemi());
        assertEquals(expectedLonSemi, p.getLonSemi());
    }

    @Test
    void testInstantAndElapsed() {
        Point p = new Point();
        Instant start = Instant.parse("2025-01-01T10:00:00Z");
        Instant current = start.plus(Duration.ofSeconds(30));

        p.setInstant(start, current);

        assertEquals(current, p.getInstant());
        assertEquals(Duration.ofSeconds(30), p.getElapsed());
        assertEquals(30.0, p.getElapsedSeconds());
    }

    @Test
    void testPutWithConverter() {
        Point p = new Point();

        // Test putting with DEGREES_CONVERTER
        p.put(PropertyKeys.lat, Converters.DEGREES_CONVERTER, 45.0);
        assertEquals(Math.toRadians(45.0), p.getLat());

        // Test putting with SEMI_CIRCLES_CONVERTER
        int semiCircles = 536870912; // Approximately 45 degrees
        p.put(PropertyKeys.lon, Converters.SEMI_CIRCLES_CONVERTER, semiCircles);
        double expectedRad = semiCircles * Math.PI / (1L << 31);
        assertEquals(expectedRad, p.getLon(), 0.00001);

        // Test with null value
        p.put(PropertyKeys.power, Converters.NOOP_CONVERTER, null);
        assertNull(p.getPower());
    }

    @Test
    void testGetWithConverter() {
        Point p = new Point();
        p.setLat(Math.toRadians(60.0));

        // Test getting with DEGREES_CONVERTER
        Double degrees = p.get(PropertyKeys.lat, Converters.DEGREES_CONVERTER);
        assertEquals(60.0, degrees, 0.001);

        // Test getting with SEMI_CIRCLES_CONVERTER
        Integer semi = p.get(PropertyKeys.lat, Converters.SEMI_CIRCLES_CONVERTER);
        int expected = (int) (60.0 * (1L << 31) / 180.0);
        assertEquals(expected, semi);

        // Test with null value
        Point p2 = new Point();
        assertNull(p2.get(PropertyKeys.power, Converters.NOOP_CONVERTER));
    }

    @Test
    void testDistanceTo() {
        Point p1 = new Point();
        p1.setLat(Math.toRadians(48.8566)); // Paris
        p1.setLon(Math.toRadians(2.3522));

        Point p2 = new Point();
        p2.setLat(Math.toRadians(51.5074)); // London
        p2.setLon(Math.toRadians(-0.1278));

        double distance = p1.distanceTo(p2);
        // Distance between Paris and London is approximately 344 km
        assertTrue(distance > 340000 && distance < 350000);

        // Test distance to same point
        assertEquals(0.0, p1.distanceTo(p1), 0.001);
    }

    @Test
    void testInterpolate() {
        Point p1 = new Point();
        p1.setLat(Math.toRadians(0.0));
        p1.setLon(Math.toRadians(0.0));
        p1.setEle(100.0);
        p1.setPower(200.0);
        p1.setSpeed(5.0);

        Point p2 = new Point();
        p2.setLat(Math.toRadians(10.0));
        p2.setLon(Math.toRadians(10.0));
        p2.setEle(200.0);
        p2.setPower(300.0);
        p2.setSpeed(10.0);

        // Interpolate at 0.5 (middle)
        Point middle = Point.interpolate(p1, p2, 0.5);
        assertEquals(Math.toRadians(5.0), middle.getLat(), 0.001);
        assertEquals(Math.toRadians(5.0), middle.getLon(), 0.001);
        assertEquals(150.0, middle.getEle());
        assertEquals(250.0, middle.getPower());
        assertEquals(7.5, middle.getSpeed());

        // Interpolate at 0.0 (start)
        Point start = Point.interpolate(p1, p2, 0.0);
        assertEquals(p1.getLat(), start.getLat());
        assertEquals(p1.getEle(), start.getEle());

        // Interpolate at 1.0 (end)
        Point end = Point.interpolate(p1, p2, 1.0);
        assertEquals(p2.getLat(), end.getLat());
        assertEquals(p2.getEle(), end.getEle());
    }

    @Test
    void testInterpolateWithNullValues() {
        Point p1 = new Point();
        p1.setLat(Math.toRadians(0.0));
        p1.setPower(200.0);
        // speed is null

        Point p2 = new Point();
        p2.setLat(Math.toRadians(10.0));
        // power is null
        p2.setSpeed(10.0);

        Point middle = Point.interpolate(p1, p2, 0.5);
        assertEquals(Math.toRadians(5.0), middle.getLat(), 0.001);
        assertNull(middle.getPower()); // null because p2.power is null
        assertNull(middle.getSpeed()); // null because p1.speed is null
    }

    @Test
    void testCopy() {
        Point original = new Point();
        original.setLat(45.0);
        original.setLon(90.0);
        original.setEle(1000.0);
        original.setPower(250.0);

        Point copy = original.copy();

        assertEquals(original.getLat(), copy.getLat());
        assertEquals(original.getLon(), copy.getLon());
        assertEquals(original.getEle(), copy.getEle());
        assertEquals(original.getPower(), copy.getPower());

        // Verify it's a deep copy
        original.setLat(50.0);
        assertEquals(45.0, copy.getLat());
    }

    @Test
    void testProject() {
        Point p = new Point();
        p.setLat(Math.toRadians(45.0));
        p.setLon(Math.toRadians(90.0));

        var vector = p.project();

        assertNotNull(vector);
        // Just verify it returns reasonable values
        assertTrue(vector.x() > 0);
        assertTrue(vector.y() > 0);
        assertEquals(0, vector.z());
    }

    @Test
    void testGetGpxData() {
        Point p = new Point();
        p.setLat(Math.toRadians(45.5));
        p.setLon(Math.toRadians(2.3));
        p.setEle(100.0);
        p.setPower(250.0);
        p.setSpeed(8.33);

        Map<String, String> gpxData = p.getGpxData();

        assertNotNull(gpxData);
        assertTrue(gpxData.size() > 0);
        // Check that values are formatted as strings
        assertTrue(gpxData.containsKey("power"));
        assertEquals("250.0", gpxData.get("power"));
    }

    @Test
    void testToString() {
        Point p = new Point();
        p.setLat(Math.toRadians(45.0));
        p.setLon(Math.toRadians(90.0));
        p.setEle(100.0);
        p.setPower(250.0);

        String str = p.toString();

        assertNotNull(str);
        assertTrue(str.contains("lat"));
        assertTrue(str.contains("lon"));
        assertTrue(str.contains("ele"));
        assertTrue(str.contains("power"));
        assertTrue(str.contains("45")); // Should contain formatted latitude
        assertTrue(str.contains("90")); // Should contain formatted longitude
        assertTrue(str.contains("100")); // Should contain elevation
        assertTrue(str.contains("250")); // Should contain power
    }

    @Test
    void testToStringWithNullValues() {
        Point p = new Point();
        p.setLat(Math.toRadians(45.0));
        // Other values are null

        String str = p.toString();

        assertNotNull(str);
        assertTrue(str.contains("lat"));
        assertFalse(str.contains("power")); // Should not include null values
        assertFalse(str.contains("ele")); // Should not include null values
    }

    @Test
    void testGetHuman() {
        Point p = new Point();
        p.setPower(250.5);

        // The getHuman method is private, but we test it through toString
        String str = p.toString();
        assertTrue(str.contains("[power]=250.5"));
    }

    @Test
    void testPutDebug() {
        Point p = new Point();

        // Save original debug state
        boolean originalDebug = Constants.DEBUG;

        try {
            // Test with DEBUG = false (should not set value)
            // Note: Constants.DEBUG is final, so we can't change it at runtime
            // This test verifies the method exists and doesn't throw
            p.putDebug(PropertyKeys.power, 100.0);

            // If DEBUG is true, value should be set
            if (Constants.DEBUG) {
                assertEquals(100.0, p.getPower());
            }
            // If DEBUG is false, value might not be set (depends on Constants.DEBUG)

        } finally {
            // No cleanup needed for final field
        }
    }

    @Test
    void testEqualsAndHashCode() {
        Point p1 = new Point();
        p1.setLat(45.0);
        p1.setLon(90.0);
        p1.setEle(100.0);

        Point p2 = new Point();
        p2.setLat(45.0);
        p2.setLon(90.0);
        p2.setEle(100.0);

        Point p3 = new Point();
        p3.setLat(45.0);
        p3.setLon(90.0);
        p3.setEle(200.0); // Different elevation

        // Test equals
        assertEquals(p1, p2);
        assertNotEquals(p1, p3);
        assertEquals(p1, p1);
        assertNotEquals(p1, null);
        assertNotEquals(p1, "not a point");

        // Test hashCode
        assertEquals(p1.hashCode(), p2.hashCode());
        // p1 and p3 might have different hashCodes (not guaranteed but likely)
    }

    @Test
    void testEdgeCasesForDistanceTo() {
        Point p1 = new Point();
        Point p2 = new Point();

        // Test antipodal points (opposite sides of Earth)
        p1.setLat(Math.toRadians(90.0)); // North pole
        p1.setLon(0.0);
        p2.setLat(Math.toRadians(-90.0)); // South pole
        p2.setLon(0.0);

        double distance = p1.distanceTo(p2);
        // Half circumference of Earth (approximately 20,000 km)
        assertTrue(distance > 19000000 && distance < 21000000);

        // Test very close points
        p1.setLat(Math.toRadians(45.0));
        p1.setLon(Math.toRadians(2.0));
        p2.setLat(Math.toRadians(45.0001));
        p2.setLon(Math.toRadians(2.0001));

        distance = p1.distanceTo(p2);
        // Should be very small (less than 20 meters)
        assertTrue(distance < 20);
    }
}
