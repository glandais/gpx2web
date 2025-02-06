package io.github.glandais;

import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.io.read.GPXFileReader;
import io.github.glandais.srtm.GPXElevationFixer;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FileUtils;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

@RequiredArgsConstructor
//@Path("/profile")
public class ProfileController {

    private final GPXFileReader gpxFileReader;

    private final GPXElevationFixer gpxElevationFixer;

//    private final GPXCharter gpxCharter;
/*
    @POST
    @Consumes(MediaType.WILDCARD)
    public Response handleFileUpload(InputStream stream,
                                     @QueryParam("width") Integer width,
                                     @QueryParam("height") Integer height) throws Exception {
        List<GPXPath> paths = gpxFileReader.parseGpx(stream);
        if (paths.size() == 1) {
            GPXPath gpxPath = paths.get(0);
            gpxElevationFixer.fixElevation(gpxPath, true);

            File tmp = File.createTempFile("chart", "tmp");
//            gpxCharter.createChartWeb(gpxPath, tmp, width, height);
            byte[] bytes = FileUtils.readFileToByteArray(tmp);
            Files.delete(tmp.toPath());

            return Response.ok(bytes, "image/png")
                    .header("Content-Disposition", "attachment;filename=activity.png")
                    .header("Content-Length", bytes.length)
                    .build();
        } else {
            throw new IllegalArgumentException("0 or more than 1 path found");
        }
    }
*/
}
