package io.github.glandais.gpx.web.resource.template;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/parameters")
public class ParametersPageResource {

    @Inject
    Template parameters;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return parameters.data("title", "GPX Virtualizer");
    }
}
