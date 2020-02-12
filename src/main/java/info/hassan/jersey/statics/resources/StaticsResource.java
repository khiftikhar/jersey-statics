package info.hassan.jersey.statics.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import info.hassan.jersey.statics.api.StaticResource;
import info.hassan.jersey.statics.services.StaticResourceService;

@Path("/")
public class StaticsResource {

  private StaticResourceService staticResourceService;

  public void setStaticResourceService(final StaticResourceService staticResourceService) {
    this.staticResourceService = staticResourceService;
  }

  @GET
  @Produces("text/html")
  @Path("/{parameter: |index|index.html}")
  public Response index() {
    return buildResponseFromStaticsResponse(staticResourceService.getDataForResource("index.html"));
  }

  @Path("/{anyResource:.*}")
  @GET
  public Response getResource(@PathParam("anyResource") final String anyResource) {
    return buildResponseFromStaticsResponse(staticResourceService.getDataForResource(anyResource));
  }

  private Response buildResponseFromStaticsResponse(final StaticResource response) {
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
