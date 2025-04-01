package io.github.glandais.gpx.io;

import io.github.glandais.gpx.Context;
import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.io.read.GPXFileReader;
import java.io.File;
import org.junit.jupiter.api.Test;

class ReadWriteTest {

    @Test
    void readWriteTest() throws Exception {
        GPX gpx = new GPXFileReader().parseGPX(ReadWriteTest.class.getResourceAsStream("/strava.gpx"));
        String output = "strava";
        Context.INSTANCE.getGpxFileWriter().writeGPX(gpx, new File("target/" + output + ".gpx"), true);
        Context.INSTANCE.getJsonFileWriter().writeGPXPath(gpx.paths().get(0), new File("target/" + output + ".json"));
        Context.INSTANCE.getFitFileWriter().writeGPX(gpx, new File("target/" + output + ".fit"));
        Context.INSTANCE.getCsvFileWriter().writeGPXPath(gpx.paths().get(0), new File("target/" + output + ".fit"));
        Context.INSTANCE.getSrtmMapProducer().createSRTMMap(new File("target/" + output + "-srtm.png"), gpx, 1024, 0.1);
        Context.INSTANCE
                .getTileMapProducer()
                .createTileMap(
                        new File("target/" + output + "-map.png"),
                        gpx,
                        "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
                        0.2,
                        1024,
                        768);
    }
}
