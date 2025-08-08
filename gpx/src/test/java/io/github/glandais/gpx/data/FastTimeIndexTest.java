package io.github.glandais.gpx.data;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class FastTimeIndexTest {

    @Test
    void testEmptyPath() {
        GPXPath emptyPath = new GPXPath("Empty", GPXPathType.TRACK);
        emptyPath.setPoints(new ArrayList<>());

        FastTimeIndex index = new FastTimeIndex(emptyPath);

        assertEquals(0, index.size());
        assertTrue(index.isSorted());
        assertEquals(-1, index.findTimeIndex(0.0));
        assertEquals(-1, index.findTimeIndex(10.0));
        assertTrue(Double.isNaN(index.getElapsedTime(0)));
        assertTrue(Double.isNaN(index.getElapsedTime(-1)));
    }

    @Test
    void testSortedData() {
        GPXPath path = createPathWithSortedTimes();
        FastTimeIndex index = new FastTimeIndex(path);

        assertTrue(index.isSorted());
        assertEquals(3, index.size());

        // Test exact matches
        assertEquals(0, index.findTimeIndex(0.0));
        assertEquals(1, index.findTimeIndex(10.0));
        assertEquals(2, index.findTimeIndex(20.0));

        // Test interpolated positions
        assertEquals(0, index.findTimeIndex(5.0)); // Between 0 and 10
        assertEquals(1, index.findTimeIndex(15.0)); // Between 10 and 20

        // Test edge cases
        assertEquals(-1, index.findTimeIndex(-5.0)); // Before first
        assertEquals(2, index.findTimeIndex(25.0)); // After last

        // Test getElapsedTime
        assertEquals(0.0, index.getElapsedTime(0));
        assertEquals(10.0, index.getElapsedTime(1));
        assertEquals(20.0, index.getElapsedTime(2));
    }

    @Test
    void testUnsortedData() {
        GPXPath path = createPathWithUnsortedTimes();
        FastTimeIndex index = new FastTimeIndex(path);

        assertFalse(index.isSorted());
        assertEquals(3, index.size());

        // Should fall back to linear search
        assertEquals(2, index.findTimeIndex(10.0)); // Finds at correct position
        assertEquals(0, index.findTimeIndex(5.0));
        assertEquals(-1, index.findTimeIndex(-1.0));

        // Test that it still finds correct positions despite unsorted data
        assertEquals(2, index.findTimeIndex(15.0)); // Linear search continues through all
    }

    @Test
    void testEmptyIndexCase() {
        // Test FastTimeIndex with empty path (null case simulation)
        GPXPath path = new GPXPath("Test", GPXPathType.TRACK);
        path.setPoints(new ArrayList<>());

        FastTimeIndex index = new FastTimeIndex(path);

        assertEquals(0, index.size());
        assertTrue(index.isSorted());
        assertTrue(Double.isNaN(index.getElapsedTime(0)));
        assertEquals(-1, index.findTimeIndex(5.0));
    }

    @Test
    void testGetElapsedTimeOutOfBounds() {
        GPXPath path = createPathWithSortedTimes();
        FastTimeIndex index = new FastTimeIndex(path);

        assertTrue(Double.isNaN(index.getElapsedTime(-1)));
        assertTrue(Double.isNaN(index.getElapsedTime(3)));
        assertTrue(Double.isNaN(index.getElapsedTime(100)));
    }

    @Test
    void testSinglePoint() {
        GPXPath path = new GPXPath("Single", GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();
        Point p = new Point();
        p.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(5)));
        p.setLat(0.0);
        p.setLon(0.0);
        points.add(p);

        path.setPoints(points);
        FastTimeIndex index = new FastTimeIndex(path);

        assertEquals(1, index.size());
        assertTrue(index.isSorted());

        assertEquals(0, index.findTimeIndex(4.0)); // Before but returns closest
        assertEquals(0, index.findTimeIndex(5.0)); // Exact
        assertEquals(0, index.findTimeIndex(6.0)); // After

        assertEquals(0.0, index.getElapsedTime(0)); // Relative to start time
    }

    @Test
    void testLinearSearchFallback() {
        // Create path with specifically unsorted times to test linear search
        GPXPath path = new GPXPath("Unsorted", GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();

        // Deliberately unsorted: 0, 20, 10, 30
        addPoint(points, baseTime, 0);
        addPoint(points, baseTime, 20);
        addPoint(points, baseTime, 10); // Out of order
        addPoint(points, baseTime, 30);

        path.setPoints(points);
        FastTimeIndex index = new FastTimeIndex(path);

        assertFalse(index.isSorted());

        // Linear search should stop at first value > target
        assertEquals(-1, index.findTimeIndex(-1.0));
        assertEquals(0, index.findTimeIndex(0.0));
        assertEquals(0, index.findTimeIndex(5.0)); // Stops at 20 > 5
        assertEquals(0, index.findTimeIndex(15.0)); // Stops at 20 > 15
        assertEquals(2, index.findTimeIndex(20.0));
        assertEquals(2, index.findTimeIndex(25.0)); // Continues through unsorted data
    }

    @Test
    void testBinarySearchPrecision() {
        // Test binary search with many points
        GPXPath path = new GPXPath("Large", GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();
        for (int i = 0; i <= 100; i++) {
            addPoint(points, baseTime, i * 10);
        }

        path.setPoints(points);
        FastTimeIndex index = new FastTimeIndex(path);

        assertTrue(index.isSorted());
        assertEquals(101, index.size());

        // Test exact matches
        assertEquals(0, index.findTimeIndex(0.0));
        assertEquals(50, index.findTimeIndex(500.0));
        assertEquals(100, index.findTimeIndex(1000.0));

        // Test between values
        assertEquals(24, index.findTimeIndex(245.0)); // Between 240 and 250
        assertEquals(49, index.findTimeIndex(499.0)); // Just before 500
        assertEquals(50, index.findTimeIndex(501.0)); // Just after 500

        // Test boundaries
        assertEquals(-1, index.findTimeIndex(-1.0));
        assertEquals(100, index.findTimeIndex(1001.0));
    }

    // Helper methods

    private GPXPath createPathWithSortedTimes() {
        GPXPath path = new GPXPath("Sorted", GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();
        addPoint(points, baseTime, 0);
        addPoint(points, baseTime, 10);
        addPoint(points, baseTime, 20);

        path.setPoints(points);
        return path;
    }

    private GPXPath createPathWithUnsortedTimes() {
        GPXPath path = new GPXPath("Unsorted", GPXPathType.TRACK);
        Instant baseTime = Instant.parse("2025-01-01T10:00:00Z");

        List<Point> points = new ArrayList<>();
        addPoint(points, baseTime, 0);
        addPoint(points, baseTime, 10);
        addPoint(points, baseTime, 5); // Out of order!

        path.setPoints(points);
        return path;
    }

    private void addPoint(List<Point> points, Instant baseTime, int elapsedSeconds) {
        Point p = new Point();
        p.setInstant(baseTime, baseTime.plus(Duration.ofSeconds(elapsedSeconds)));
        p.setLat(0.0);
        p.setLon(0.0);
        points.add(p);
    }
}
