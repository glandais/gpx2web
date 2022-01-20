package io.github.glandais;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.io.GPXParser;
import io.github.glandais.map.TileMapImage;
import io.github.glandais.map.TileMapProducer;

import javax.imageio.ImageIO;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Path("/map")
public class MapController {

    private final GPXParser gpxParser;

    private final TileMapProducer tileMapProducer;

    public MapController(final GPXParser gpxParser, final TileMapProducer tileMapProducer) {

        this.gpxParser = gpxParser;
        this.tileMapProducer = tileMapProducer;
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public Response handleFileUpload(InputStream stream,
                                     @QueryParam("tileUrl") String tileUrl,
                                     @QueryParam("width") Integer width,
                                     @QueryParam("height") Integer height)
            throws Exception {
        List<GPXPath> paths = gpxParser.parsePaths(stream);
        if (paths.size() == 1) {
            TileMapImage tileMap = tileMapProducer.createTileMap(paths.get(0), tileUrl, 0, width, height);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ImageIO.write(tileMap.getImage(), "png", bos);

            byte[] bytes = bos.toByteArray();
            return Response.ok(bytes, "image/png")
                    .header("Content-Disposition", "attachment;filename=activity.png")
                    .header("Content-Length", bytes.length)
                    .build();
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

}
