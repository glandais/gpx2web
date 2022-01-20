package io.github.glandais;

import io.github.glandais.fit.FitFileWriter;
import io.github.glandais.gpx.GPXPath;
import io.github.glandais.gpx.filter.GPXFilter;
import io.github.glandais.io.GPXParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@Path("/fit")
public class FitController {

    private final GPXParser gpxParser;

    private final FitFileWriter fitFileWriter;

    private final GPXPathEnhancer gpxPathEnhancer;

    public FitController(final GPXParser gpxParser, final FitFileWriter fitFileWriter, final GPXPathEnhancer gpxPathEnhancer) {

        this.gpxParser = gpxParser;
        this.fitFileWriter = fitFileWriter;
        this.gpxPathEnhancer = gpxPathEnhancer;
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public Response handleFileUpload(
            InputStream stream,
            @QueryParam("name") String name
    ) throws Exception {
        List<GPXPath> paths = gpxParser.parsePaths(stream);
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            if (!StringUtils.isEmpty(name)) {
                gpxPath.setName(name);
            }
            gpxPathEnhancer.virtualize(gpxPath);

            GPXFilter.filterPointsDouglasPeucker(gpxPath);
            File tmp = File.createTempFile("fit", "tmp");
            fitFileWriter.writeFitFile(gpxPath, tmp);
            byte[] bytes = FileUtils.readFileToByteArray(tmp);
            Files.delete(tmp.toPath());

            return Response.ok(bytes, "application/fit")
                    .header("Content-Disposition", "attachment;filename=activity.fit")
                    .header("Content-Length", bytes.length)
                    .build();
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }

}
