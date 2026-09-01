package io.github.glandais.gpx.service.climb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.glandais.gpx.Context;
import io.github.glandais.gpx.climb.Climb;
import io.github.glandais.gpx.climb.ClimbPart;
import io.github.glandais.gpx.climb.Climbs;
import io.github.glandais.gpx.data.GPXPath;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClimbDetectorTest {

    /**
     * Elevations come from the live Mapterhorn terrain tiles, which get refreshed upstream from time
     * to time. Sub-metre drift on the DEM must not fail this test: only a change in the detected
     * climbs (their number, their boundaries, their overall shape) is a regression.
     */
    private static final double ELEVATION_DELTA = 5.0;

    /** Distances are derived from the GPX track itself, so they only move if segmentation changes. */
    private static final double DISTANCE_DELTA = 1.0;

    /** Grades are percentages; a few metres of elevation drift over kilometres stays well below this. */
    private static final double GRADE_DELTA = 0.1;

    @Test
    void getClimbs() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        List<GPXPath> gpxPaths = Context.INSTANCE
                .getGpxFileReader()
                .parseGPX(ClimbDetectorTest.class.getResourceAsStream("/ventoux.gpx"))
                .paths();
        GPXPath gpxPath = gpxPaths.get(0);

        Context.INSTANCE.getGpxPerDistance().computeOnePointPerDistance(gpxPath, 10);
        Context.INSTANCE.getGpxElevationFixer().fixElevation(gpxPath);

        List<Climb> climbs = Context.INSTANCE.getClimbDetector().getClimbs(gpxPath);

        // System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(climbs));
        Climbs expectedClimbs = objectMapper.readValue(
                ClimbDetectorTest.class.getResourceAsStream("/expectations/ventoux_climbs.json"), Climbs.class);

        assertClimbsEquals(expectedClimbs, climbs, "climbs");

        Climb climbShift = climbs.get(0).shiftDist(100.0);
        // System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(climbShift));
        Climb expectedClimb = objectMapper.readValue(
                ClimbDetectorTest.class.getResourceAsStream("/expectations/ventoux_climbShift.json"), Climb.class);
        assertClimbEquals(expectedClimb, climbShift, "climbShift");
    }

    private static void assertClimbsEquals(List<Climb> expected, List<Climb> actual, String context) {
        assertEquals(expected.size(), actual.size(), context + ": number of climbs");
        for (int i = 0; i < expected.size(); i++) {
            assertClimbEquals(expected.get(i), actual.get(i), context + "[" + i + "]");
        }
    }

    private static void assertClimbEquals(Climb expected, Climb actual, String context) {
        assertEquals(expected.startDist(), actual.startDist(), DISTANCE_DELTA, context + ".startDist");
        assertEquals(expected.endDist(), actual.endDist(), DISTANCE_DELTA, context + ".endDist");
        assertEquals(expected.dist(), actual.dist(), DISTANCE_DELTA, context + ".dist");
        assertEquals(expected.startEle(), actual.startEle(), ELEVATION_DELTA, context + ".startEle");
        assertEquals(expected.endEle(), actual.endEle(), ELEVATION_DELTA, context + ".endEle");
        assertEquals(expected.elevation(), actual.elevation(), ELEVATION_DELTA, context + ".elevation");
        assertEquals(
                expected.positiveElevation(),
                actual.positiveElevation(),
                ELEVATION_DELTA,
                context + ".positiveElevation");
        assertEquals(
                expected.negativeElevation(),
                actual.negativeElevation(),
                ELEVATION_DELTA,
                context + ".negativeElevation");
        assertEquals(expected.grade(), actual.grade(), GRADE_DELTA, context + ".grade");
        assertEquals(expected.climbingGrade(), actual.climbingGrade(), GRADE_DELTA, context + ".climbingGrade");

        assertEquals(expected.parts().size(), actual.parts().size(), context + ": number of parts");
        for (int i = 0; i < expected.parts().size(); i++) {
            assertClimbPartEquals(expected.parts().get(i), actual.parts().get(i), context + ".parts[" + i + "]");
        }
    }

    private static void assertClimbPartEquals(ClimbPart expected, ClimbPart actual, String context) {
        assertEquals(expected.startDist(), actual.startDist(), DISTANCE_DELTA, context + ".startDist");
        assertEquals(expected.endDist(), actual.endDist(), DISTANCE_DELTA, context + ".endDist");
        assertEquals(expected.dist(), actual.dist(), DISTANCE_DELTA, context + ".dist");
        assertEquals(expected.startEle(), actual.startEle(), ELEVATION_DELTA, context + ".startEle");
        assertEquals(expected.endEle(), actual.endEle(), ELEVATION_DELTA, context + ".endEle");
        assertEquals(expected.ele(), actual.ele(), ELEVATION_DELTA, context + ".ele");
        assertEquals(expected.grade(), actual.grade(), GRADE_DELTA, context + ".grade");
    }
}
