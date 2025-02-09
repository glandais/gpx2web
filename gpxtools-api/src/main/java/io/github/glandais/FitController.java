package io.github.glandais;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.io.write.FitFileWriter;
import io.github.glandais.virtual.GPXPathEnhancer;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@Path("/fit")
public class FitController {

    private final GPXFileReader gpxFileReader;

    private final FitFileWriter fitFileWriter;

    private final GPXPathEnhancer gpxPathEnhancer;

    public FitController(final GPXFileReader gpxFileReader, final FitFileWriter fitFileWriter, final GPXPathEnhancer gpxPathEnhancer) {

        this.gpxFileReader = gpxFileReader;
        this.fitFileWriter = fitFileWriter;
        this.gpxPathEnhancer = gpxPathEnhancer;
    }

    @POST
    @Consumes(MediaType.WILDCARD)
    public Response handleFileUpload(
            InputStream stream,
            @QueryParam("name") String name
    ) throws Exception {
        List<GPXPath> paths = gpxFileReader.parseGpx(stream).paths();
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            if (name != null && !name.isEmpty()) {
                gpxPath.setName(name);
            }
            gpxPathEnhancer.virtualize(gpxPath);

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
