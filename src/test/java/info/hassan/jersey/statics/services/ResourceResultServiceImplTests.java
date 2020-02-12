package info.hassan.jersey.statics.services;

import static info.hassan.jersey.statics.services.TestsHelper.APPLICATION_JAVASCRIPT;
import static info.hassan.jersey.statics.services.TestsHelper.APPLICATION_JSON;
import static info.hassan.jersey.statics.services.TestsHelper.IMAGE_PNG;
import static info.hassan.jersey.statics.services.TestsHelper.IMAGE_SVG;
import static info.hassan.jersey.statics.services.TestsHelper.TEXT_CSS;
import static info.hassan.jersey.statics.services.TestsHelper.TEXT_HTML;
import static info.hassan.jersey.statics.services.TestsHelper.assertEverythingInResource;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.condition.OS.MAC;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import info.hassan.jersey.statics.api.ResourceResult;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

@DisplayName("Testing default ResourceServiceImpl")
@DisabledOnOs({WINDOWS, MAC})
class ResourceResultServiceImplTests {

  private ResourceService service;
  private ResourceResult defaultNotFound;
  private ResourceResult defaultServerError;

  @BeforeEach
  void setup() throws FileNotFoundException {
    service = new ResourceServiceImpl(Paths.get("src", "test", "resources", "html"));
    assertNotNull(service);
    defaultNotFound = service.getDataForResource("404.html");
    defaultServerError = service.getDataForResource("500.html");
  }

  @Nested
  @DisplayName("Testing scenarios when things goes wrong")
  class BadBaseDirTests {

    @DisplayName("If baseDir is null, IllegalArgumentException is thrown")
    @Test
    void throwIllegalArgumentException() {
      try {
        service = new ResourceServiceImpl(null);
        fail("Exception must been thrown");
      } catch (Exception e) {
        assertTrue(e instanceof IllegalArgumentException);
      }
    }

    @DisplayName("If baseDir doesn't exist, FileNotFoundException is thrown")
    @Test
    void throwFileNotFoundException() {
      try {
        service = new ResourceServiceImpl(Paths.get("/some/bad/path"));
        fail("Exception must been thrown");
      } catch (Exception e) {
        assertTrue(e instanceof FileNotFoundException);
      }
    }

    @DisplayName(
        "When requested resource and baseDir/404.html doesn't exists , default 404.html is returned")
    @Test
    void resourceNotFoundDefault() {
      final ResourceResult notFound = service.getDataForResource("abc/xyz/123.html");
      assertEverythingInResource(notFound, 404, TEXT_HTML);
      assertEquals(defaultNotFound, notFound);
    }

    @DisplayName("When default 500.html doesn't exists, default 505.html is returned")
    @Test
    void resourceServerErrorDefault() {
      final ResourceResult serverError = service.getDataForResource("500.html");
      assertEverythingInResource(serverError, 500, TEXT_HTML);
      assertEquals(defaultServerError, serverError);
    }

    @DisplayName("Asking for a resource that doesn't exists, shall return 404.html")
    @Test
    void resourceNotFound() throws IOException {
      final byte[] data =
          "<html><body><p>404 - Page not found</p></body></html>"
              .getBytes(Charset.defaultCharset());
      final Path new404 =
          Files.write(
              Paths.get(service.getBaseDir().toString(), "404.html"),
              data,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING);
      service = new ResourceServiceImpl(Paths.get("src", "test", "resources", "html"));
      assertNotNull(service);
      assertNotEquals(defaultNotFound, service.getDataForResource("404.html"));
      Files.deleteIfExists(new404);
    }
  }

  @DisplayName("Testing the service by getting resources placed at different sub levels")
  @Nested
  class LoadingExistingResourcesThatExist {

    @DisplayName("Loading the resource from baseDir")
    @Test
    void gettingResources() {
      assertEverythingInResource(service.getDataForResource("index.html"), 200, TEXT_HTML);
    }

    @DisplayName("Loading the sub-resource from baseDir's sub-directory")
    @Test
    void gettingSubResource() {
      assertEverythingInResource(service.getDataForResource("css/main.css"), 200, TEXT_CSS);
    }

    @DisplayName("Trying different resources from different sub-directory under baseDir")
    @Test
    void checkDifferentSubResources() {
      assertEverythingInResource(service.getDataForResource("js/app.json"), 200, APPLICATION_JSON);
      assertEverythingInResource(
          service.getDataForResource("js/main.js"), 200, APPLICATION_JAVASCRIPT);
      assertEverythingInResource(service.getDataForResource("img/home.svg"), 200, IMAGE_SVG);
      assertEverythingInResource(service.getDataForResource("img/home.png"), 200, IMAGE_PNG);
    }
  }

  @DisplayName("Testing that is not updated even if a resource is updated")
  @Nested
  class TestingCacheUsage {

    @DisplayName("Updated index.html will not be returned")
    @Test
    void indexFileUpdateHaveNoEffect() throws IOException {
      ResourceResult index = service.getDataForResource("index.html");
      assertEverythingInResource(index, 200, TEXT_HTML);
      final int indexLength = index.getData().length;
      final Document document = Jsoup.parse(new String(index.getData(), Charset.defaultCharset()));
      document.head().append("<script id='to-delete'>console.log('Adding test script');</script>");
      final String html = document.html();
      Files.write(
          service.getBaseDir().resolve("index.html"),
          html.getBytes(Charset.defaultCharset()),
          StandardOpenOption.TRUNCATE_EXISTING);
      index = service.getDataForResource("index.html");
      assertEquals(indexLength, index.getData().length);
      Files.write(
          service.getBaseDir().resolve("index.html"),
          index.getData(),
          StandardOpenOption.TRUNCATE_EXISTING);
    }
  }
}
