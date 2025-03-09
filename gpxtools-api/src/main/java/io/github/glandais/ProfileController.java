package io.github.glandais;

import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.srtm.GPXElevationFixer;
import lombok.RequiredArgsConstructor;

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
