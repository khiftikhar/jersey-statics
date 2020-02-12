package info.hassan.jersey.statics.services;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import info.hassan.jersey.statics.api.ResourceResult;

public final class TestsHelper {
  public static final String TEXT_HTML = "text/html";
  public static final String APPLICATION_JSON = "application/json";
  public static final String APPLICATION_JAVASCRIPT = "application/javascript";
  public static final String TEXT_JAVASCRIPT = "text/javascript";
  public static final String TEXT_CSS = "text/css";
  public static final String IMAGE_SVG = "image/svg+xml";
  public static final String IMAGE_PNG = "image/png";

  public static void assertEverythingInResource(
      final ResourceResult resource, int statusCode, final String mimeType) {
    assertAll(
        "Assert that resource object is filled with correct values",
        () -> assertNotNull(resource, "Service must return resource object"),
        () -> assertEquals(statusCode, resource.getStatusCode(), "Response code must be 200"),
        () -> assertEquals(mimeType, resource.getMimeType(), "TestsHelper must be " + mimeType),
        () -> assertNotNull(resource.getData(), "Data must not be null"),
        () ->
            assertTrue(
                resource.getData().length > 0,
                "The length of data array must be creator than zero"));
  }
}
