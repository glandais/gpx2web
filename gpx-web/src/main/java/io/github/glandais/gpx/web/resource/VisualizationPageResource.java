package io.github.glandais.gpx.web.resource;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/visualization")
public class VisualizationPageResource {

    @Inject
    Template visualization;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return visualization.data("title", "GPX Virtualizer");
    }
}