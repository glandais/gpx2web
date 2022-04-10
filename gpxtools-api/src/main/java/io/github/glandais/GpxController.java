package io.github.glandais;

import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.Point;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.io.GPXFileWriter;
import io.github.glandais.io.GPXParser;
import io.github.glandais.srtm.GPXElevationFixer;
import org.apache.commons.io.FileUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@Path("/")
public class GpxController {

    private final GPXParser gpxParser;

    private final GPXPathEnhancer gpxPathEnhancer;

    private final GPXFileWriter gpxFileWriter;

    private final GPXDataComputer gpxDataComputer;

    private final GPXElevationFixer gpxElevationFixer;

    public GpxController(final GPXParser gpxParser,
                         final GPXPathEnhancer gpxPathEnhancer,
                         final GPXFileWriter gpxFileWriter,
                         final GPXDataComputer gpxDataComputer,
                         final GPXElevationFixer gpxElevationFixer) {

        this.gpxParser = gpxParser;
        this.gpxPathEnhancer = gpxPathEnhancer;
        this.gpxFileWriter = gpxFileWriter;
        this.gpxDataComputer = gpxDataComputer;
        this.gpxElevationFixer = gpxElevationFixer;
    }

    @Path("/simplify")
    @POST
    @Consumes(MediaType.WILDCARD)
    public Response simplify(
            InputStream stream,
            @QueryParam("name") String name) throws Exception {

        List<GPXPath> paths = gpxParser.parsePaths(stream);
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            if (name != null && !name.isEmpty()) {
                gpxPath.setName(name);
            }
            gpxPathEnhancer.virtualize(gpxPath);
            GPXFilter.filterPointsDouglasPeucker(gpxPath);

            File tmp = File.createTempFile("gpx", "tmp");
            gpxFileWriter.writeGpxFile(paths, tmp);

            byte[] bytes = FileUtils.readFileToByteArray(tmp);
            Files.delete(tmp.toPath());

            return Response.ok(bytes, "application/gpx")
                    .header("Content-Disposition", "attachment;filename=activity.gpx")
                    .header("Content-Length", bytes.length)
                    .encoding("UTF-8")
                    .build();
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

    @Path("/gpxinfo")
    @POST
    @Consumes(MediaType.WILDCARD)
    public GPXInfo gpxinfo(InputStream stream) throws Exception {

        List<GPXPath> paths = gpxParser.parsePaths(stream);
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            gpxElevationFixer.fixElevation(gpxPath, true);
            float dist = Math.round(gpxPath.getDist() / 100.0) / 10.0f;

            return new GPXInfo(dist, (int) Math.round(gpxPath.getTotalElevation()), (int) Math.round(gpxPath.getTotalElevationNegative()),
                    gpxDataComputer.getWind(gpxPath), gpxDataComputer.isCrossing(gpxPath));
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

    @Path("/geojson")
    @POST
    @Consumes(MediaType.WILDCARD)
    public Response geojson(InputStream stream) throws Exception {

        List<GPXPath> paths = gpxParser.parsePaths(stream);
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            gpxPathEnhancer.virtualize(gpxPath);
            GPXFilter.filterPointsDouglasPeucker(gpxPath);
            StringBuilder bw = new StringBuilder();
            bw.append("{");
            bw.append("\n");
            bw.append("  \"type\": \"LineString\",");
            bw.append("\n");
            bw.append("  \"coordinates\": [");
            bw.append("\n");
            for (int i = 0;
                 i < gpxPath.getPoints()
                         .size();
                 i++) {
                Point point = gpxPath.getPoints()
                        .get(i);
                bw.append("    [");
                bw.append((float) point.getLonDeg());
                bw.append(", ");
                bw.append((float) point.getLatDeg());
                bw.append("]");
                if (i != gpxPath.getPoints()
                        .size() - 1) {
                    bw.append(",");
                }
                bw.append("\n");
            }
            bw.append("	]");
            bw.append("\n");
            bw.append("}");
            bw.append("\n");

            byte[] bytes = bw.toString().getBytes(StandardCharsets.UTF_8);

            return Response.ok(bytes, "application/geojson")
                    .header("Content-Disposition", "attachment;filename=activity.json")
                    .header("Content-Length", bytes.length)
                    .encoding("UTF-8")
                    .build();
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }
}
