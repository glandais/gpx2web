package io.github.glandais.gpx.web.resource.api;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.data.GPXPath;
import io.github.glandais.gpx.data.Point;
import io.github.glandais.gpx.io.read.GPXFileReader;
import io.github.glandais.gpx.web.model.GPXAnalysisResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/api/analyze")
public class GPXAnalysisResource {

    @Inject
    GPXFileReader gpxFileReader;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response analyzeGpx(@RestForm("gpxFile") FileUpload gpxFile) {
        try {
            GPX gpx = gpxFileReader.parseGPX(gpxFile.uploadedFile().toFile());

            // Validate single track
            if (gpx.paths().size() != 1) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("GPX file must contain exactly one track, found: "
                                + gpx.paths().size())
                        .build();
            }

            GPXPath gpxPath = gpx.paths().get(0);

            // Calculate total distance
            double totalDistance = 0.0;
            if (!gpxPath.getPoints().isEmpty()) {
                Point lastPoint = gpxPath.getPoints().get(gpxPath.getPoints().size() - 1);
                totalDistance = lastPoint.getDist();
            }

            GPXAnalysisResponse analysis =
                    new GPXAnalysisResponse(totalDistance, gpxPath.getPoints().size());

            return Response.ok(analysis).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error analyzing GPX file: " + e.getMessage())
                    .build();
        }
    }
}
