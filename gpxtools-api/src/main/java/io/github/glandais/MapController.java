package io.github.glandais;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Path("/map")
public class MapController {

    private final GPXFileReader gpxFileReader;

    private final TileMapProducer tileMapProducer;

    public MapController(final GPXFileReader gpxFileReader, final TileMapProducer tileMapProducer) {

        this.gpxFileReader = gpxFileReader;
        this.tileMapProducer = tileMapProducer;
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public Response handleFileUpload(InputStream stream,
                                     @QueryParam("tileUrl") String tileUrl,
                                     @QueryParam("width") Integer width,
                                     @QueryParam("height") Integer height)
            throws Exception {
        GPX gpx = gpxFileReader.parseGpx(stream);
        TileMapImage tileMap = tileMapProducer.createTileMap(gpx.paths().get(0), tileUrl, 0, width, height);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(tileMap.getImage(), "png", bos);

        byte[] bytes = bos.toByteArray();
        return Response.ok(bytes, "image/png")
                .header("Content-Disposition", "attachment;filename=activity.png")
                .header("Content-Length", bytes.length)
                .build();
    }

}
