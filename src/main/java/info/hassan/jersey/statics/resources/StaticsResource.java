package info.hassan.jersey.statics.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import info.hassan.jersey.statics.api.ResourceResult;
import info.hassan.jersey.statics.services.ResourceService;

@Path("/")
public class StaticsResource {

  private ResourceService resourceService;

  public void setResourceService(final ResourceService resourceService) {
    this.resourceService = resourceService;
  }

  @GET
  @Produces("text/html")
  @Path("/{parameter: |index|index.html}")
  public Response index() {
    return buildResponseFromStaticsResponse(resourceService.getDataForResource("index.html"));
  }

  @Path("/{anyResource:.*}")
  @GET
  public Response getResource(@PathParam("anyResource") final String anyResource) {
    return buildResponseFromStaticsResponse(resourceService.getDataForResource(anyResource));
  }

  private Response buildResponseFromStaticsResponse(final ResourceResult response) {
    final Response.ResponseBuilder builder = Response.status(response.getStatusCode());
    if (response.getStatusCode() == 200) {
      return builder.type(response.getMimeType()).entity(response.getData()).build();
    } else {
      if (response.hasMimeType()) {
        builder.type(response.getMimeType());
      }
      if (response.hasData()) {
        builder.entity(response.getData());
      }
      return builder.build();
    }
  }
}
