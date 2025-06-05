package io.github.glandais.gpx.web.resource.api;

import io.github.glandais.gpx.web.model.VirtualizationRequest;
import io.github.glandais.gpx.web.model.VirtualizationResponse;
import io.github.glandais.gpx.web.service.VirtualizationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/api/virtualize")
public class GPXVirtualizationResource {

    @Inject
    VirtualizationService virtualizationService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response virtualizeGpx(
            @RestForm("gpxFile") FileUpload gpxFile,
            @RestForm("parameters") @PartType(MediaType.APPLICATION_JSON) VirtualizationRequest parameters) {
        try {
            VirtualizationResponse response = virtualizationService.virtualizeGpx(gpxFile, parameters);
            return Response.ok(response).build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error processing GPX file: " + e.getMessage())
                    .build();
        }
    }
}
