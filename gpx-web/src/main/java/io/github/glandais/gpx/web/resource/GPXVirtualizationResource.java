package io.github.glandais.gpx.web.resource;

import io.github.glandais.gpx.data.GPX;
import io.github.glandais.gpx.io.write.GPXFileWriter;
import io.github.glandais.gpx.web.model.VirtualizationRequest;
import io.github.glandais.gpx.web.service.VirtualizationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.jboss.resteasy.reactive.MultipartForm;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;

@Path("/api/virtualize")
public class GPXVirtualizationResource {

    @Inject
    VirtualizationService virtualizationService;

    @Inject
    GPXFileWriter gpxFileWriter;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("application/gpx+xml")
    public Response virtualizeGpx(@MultipartForm VirtualizationForm form) {
        try {
            GPX virtualizedGpx = virtualizationService.virtualizeGpx(form.gpxFile, form.parameters);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            gpxFileWriter.writeGPX(virtualizedGpx, outputStream);

            return Response.ok(outputStream.toByteArray())
                    .header("Content-Disposition", "attachment; filename=\"virtualized.gpx\"")
                    .header("Content-Type", "application/gpx+xml")
                    .build();

        } catch (IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error processing GPX file: " + e.getMessage())
                    .build();
        }
    }

    public static class VirtualizationForm {
        @RestForm("gpxFile")
        public byte[] gpxFile;

        @RestForm("parameters")
        @PartType(MediaType.APPLICATION_JSON)
        public VirtualizationRequest parameters;
    }
}