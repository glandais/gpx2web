package io.github.glandais.gpx.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.glandais.gpx.data.values.PropertyKeys;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GPXPathAverageTest {

    private GPXPath gpxPath;
    private Instant baseTime;

    @BeforeEach
    void setUp() {
        gpxPath = new GPXPath("Test Path", GPXPathType.TRACK);
        baseTime = Instant.parse("2025-01-01T10:00:00Z");

        // Create test points: (elapsed_seconds, power_value)
        // (0,10), (10,20), (15,30)
        List<Point> points = new ArrayList<>();

        Point p1 = new Point();
        p1.setInstant(baseTime, baseTime); // elapsed = 0
        p1.setPower(10.0);
        p1.setHeartRate(60.0);
        p1.setLat(0.0);
        p1.setLon(0.0);
        p1.setEle(100.0);
        points.add(p1);

        Point p2 = new Point();
        p2.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(10))); // elapsed = 10
        p2.setPower(20.0);
        p2.setHeartRate(70.0);
        p2.setLat(0.0);
        p2.setLon(0.0);
        p2.setEle(110.0);
        points.add(p2);

        Point p3 = new Point();
        p3.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(15))); // elapsed = 15
        p3.setPower(30.0);
        p3.setHeartRate(80.0);
        p3.setLat(0.0);
        p3.setLon(0.0);
        p3.setEle(120.0);
        points.add(p3);

        gpxPath.setPoints(points);
    }

    @Test
    void testSimpleCase() {
        // Simple test: constant value
        GPXPath gpxPath = new GPXPath("Test", GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();

        Point p1 = new Point();
        p1.setInstant(baseTime, baseTime);
        p1.setPower(100.0);
        p1.setLat(0.0);
        p1.setLon(0.0);
        points.add(p1);

        Point p2 = new Point();
        p2.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(10)));
        p2.setPower(100.0);
        p2.setLat(0.0);
        p2.setLon(0.0);
        points.add(p2);

        gpxPath.setPoints(points);

        double average = gpxPath.getAverage(0.0, 10.0, PropertyKeys.power);
        assertEquals(100.0, average, 0.001);
    }

    @Test
    void testGetAverageExample() {
        // Test the example: (0,10),(10,20),(15,30) average between 5 and 12
        // Expected interpolated points: (5,15),(10,20),(12,24)
        // Using trapezoidal integration:
        // From 5 to 10: (15+20)/2 * (10-5) = 17.5 * 5 = 87.5
        // From 10 to 12: (20+24)/2 * (12-10) = 22 * 2 = 44
        // Total area = 87.5 + 44 = 131.5
        // Average = 131.5 / (12-5) = 131.5 / 7 ≈ 18.79

        double average = gpxPath.getAverage(5.0, 12.0, PropertyKeys.power);

        assertTrue(Math.abs(average - 18.79) < 0.01, "Average should be approximately 18.79, got: " + average);
    }

    @Test
    void testGetAverageInterpolationStart() {
        // Test interpolation at start point
        double average = gpxPath.getAverage(5.0, 5.1, PropertyKeys.power);

        // At time 5, power should be interpolated as 15
        assertTrue(Math.abs(average - 15.0) < 0.1, "Average should be approximately 15.0, got: " + average);
    }

    @Test
    void testGetAverageInterpolationEnd() {
        // Test interpolation at end point
        double average = gpxPath.getAverage(11.9, 12.0, PropertyKeys.power);

        // At time 12, power should be interpolated as 24
        // Very short interval, so average should be close to 24
        assertTrue(Math.abs(average - 24.0) < 0.5, "Average should be approximately 24.0, got: " + average);
    }

    @Test
    void testGetAverageExactPoints() {
        // Test average between exact data points
        double average = gpxPath.getAverage(0.0, 10.0, PropertyKeys.power);

        // Trapezoidal integration: (10+20)/2 * 10 / 10 = 15.0
        assertEquals(15.0, average, 0.001);
    }

    @Test
    void testGetAverageSpanMultiplePoints() {
        // Test average spanning all points
        double average = gpxPath.getAverage(0.0, 15.0, PropertyKeys.power);

        // Trapezoidal integration:
        // From 0 to 10: (10+20)/2 * 10 = 150
        // From 10 to 15: (20+30)/2 * 5 = 125
        // Total area = 150 + 125 = 275
        // Average = 275 / 15 ≈ 18.33
        assertTrue(Math.abs(average - 18.33) < 0.1, "Average should be approximately 18.33, got: " + average);
    }

    @Test
    void testGetAverageHeartRate() {
        // Test with different property (heart rate)
        double average = gpxPath.getAverage(5.0, 12.0, PropertyKeys.heartRate);

        // Interpolated HR at 5: 60 + (70-60)*(5-0)/(10-0) = 65
        // Interpolated HR at 12: 70 + (80-70)*(12-10)/(15-10) = 74
        // Similar calculation as power
        assertTrue(
                average >= 65.0 && average <= 74.0, "Heart rate average should be between 65 and 74, got: " + average);
    }

    @Test
    void testGetAverageInvalidRange() {
        // Test invalid range (from >= to)
        double average = gpxPath.getAverage(10.0, 10.0, PropertyKeys.power);
        assertEquals(20.0, average);

        average = gpxPath.getAverage(15.0, 10.0, PropertyKeys.power);
        assertEquals(25.0, average);
    }

    @Test
    void testGetAverageOutOfBounds() {
        // Test range outside data bounds
        double average = gpxPath.getAverage(20.0, 25.0, PropertyKeys.power);

        // Should use last value (30.0)
        assertEquals(30.0, average, 0.001);
    }

    @Test
    void testGetAverageBeforeStart() {
        // Test range before first data point
        double average = gpxPath.getAverage(-5.0, -1.0, PropertyKeys.power);

        // Should use first value (10.0)
        assertEquals(10.0, average, 0.001);
    }

    @Test
    void testEmptyPath() {
        GPXPath emptyPath = new GPXPath("Empty", GPXPathType.TRACK);
        emptyPath.setPoints(new ArrayList<>());

        double average = emptyPath.getAverage(0.0, 10.0, PropertyKeys.power);
        assertEquals(0.0, average);
    }

    @Test
    void testSize() {
        // Test empty path
        GPXPath emptyPath = new GPXPath("Empty", GPXPathType.TRACK);
        emptyPath.setPoints(new ArrayList<>());
        assertEquals(0, emptyPath.size());

        // Test path with points
        assertEquals(3, gpxPath.size());

        // Test adding more points
        List<Point> points = new ArrayList<>(gpxPath.getPoints());
        Point p4 = new Point();
        p4.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(20)));
        p4.setPower(40.0);
        p4.setLat(0.0);
        p4.setLon(0.0);
        p4.setEle(130.0);
        points.add(p4);

        gpxPath.setPoints(points);
        assertEquals(4, gpxPath.size());
    }
}
