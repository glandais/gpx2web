package io.github.glandais.gpx.service.climb;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.glandais.gpx.Context;
import io.github.glandais.gpx.climb.Climb;
import io.github.glandais.gpx.climb.Climbs;
import io.github.glandais.gpx.data.GPXPath;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClimbDetectorTest {

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

        assertEquals(expectedClimbs, climbs);

        Climb climbShift = climbs.get(0).shiftDist(100.0);
        // System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(climbShift));
        Climb expectedClimb = objectMapper.readValue(
                ClimbDetectorTest.class.getResourceAsStream("/expectations/ventoux_climbShift.json"), Climb.class);
        assertEquals(expectedClimb, climbShift);
    }
}
